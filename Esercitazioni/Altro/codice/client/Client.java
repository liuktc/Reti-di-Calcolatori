import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Client {

	//argomenti: Client serverIP serverPort threshold
	public static void main(String[] args) {
		int serverPort=-1, soglia=-1;
		InetAddress serverIP = null;
		if(args.length==3) {
			try {
				serverIP = InetAddress.getByName(args[0]);
			} catch (UnknownHostException e) {
				System.out.println("Host non esistente");
				System.exit(2);
			}
			try {
				serverPort = Integer.parseInt(args[1]);
				soglia=Integer.parseInt(args[2]);
			}
			catch(NumberFormatException e) {
				System.out.println("Usage: PutFileClient serverIP serverPort threshold");
				System.exit(3);
			}
		}
		else {
			System.out.println("Usage: PutFileClient serverIP serverPort threshold");
			System.exit(1);
		}
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String msgType=null;
		String fileName=null;
		Socket socket=null;
		DataInputStream inSock=null;
		DataOutputStream outSock=null;
		String dir=null;
		String[] dirs=null;
		File[] d=null;
		FileInputStream inFile=null;
		FileOutputStream outFile=null;
		int countDir = 0, countFiles=0;
		long fileDim=0;
		File curFile=null;
		
		//creazione della socket lato client
		try {
			socket=new Socket(serverIP, serverPort);
			socket.setSoTimeout(30000);
			System.out.println("Creata la socket "+socket);
		}
		catch(Exception e){
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
		}
		
		//creazione degli stream di IO sulla socket
		try {	
			inSock = new DataInputStream(socket.getInputStream());
			outSock = new DataOutputStream(socket.getOutputStream());
		}
		catch(IOException e) {
			System.out.println("Problemi nella creazione di stream su socket");
			System.exit(5);
		}
		
		try {
			//Richiesta di inserimento del messaggio
			System.out.print("Inserire il tipo di richiesta che si vuole effettuare (mput o mget) oppure Ctrl+D (UNIX)/Ctrl+Z (Windows) per terminare il client: ");
			while((msgType = stdIn.readLine())!=null) {
				try {
					if(msgType.contentEquals("mput")) {
						try {
							outSock.writeUTF(msgType);
						}
						catch(Exception e) {
							System.out.println("Fallimento dell'invio del tipo di messaggio al server: ");
							e.printStackTrace();
							System.out.println("\nInserire il tipo di richiesta che si vuole effettuare (mput o mget) oppure Ctrl+D (UNIX)/Ctrl+Z (Windows) per terminare il client: ");
							continue;
						}
						
						System.out.print("Inserisci un insieme di directories separate da spazio da copiare sul server: ");
						
						//mando il numero di directories da trasferire
						countDir = 0;
						if((dir=stdIn.readLine())!=null) {
							dirs = dir.split(" ");
							d = new File[dirs.length];
							
							for(int i=0; i<dirs.length; i++) {
								d[i] = new File(dirs[i]);
								if(d[i].exists()) countDir++;
							}
							System.out.println("Invio del numero di directories da trasferire in corso al server: ");
							outSock.writeUTF(String.valueOf(countDir));
							
							//mando effettivamente tutti i file
							for(int i = 0; i<dirs.length; i++){
								if(d[i].isDirectory()) {
									System.out.println("Invio del nome della directory "+dirs[i]+" al server in corso");
									outSock.writeUTF(dirs[i]);
									System.out.println("Invio del numero di files contenuti nella directory "+dirs[i]+" in corso");
									outSock.writeUTF(String.valueOf(d[i].listFiles().length));
									for(File f: d[i].listFiles()) {
										if(f.length()<soglia) {
											System.out.println("File "+f.getName()+" con dimensione sotto la soglia specificata");
											System.out.println("\nInserire il tipo di richiesta che si vuole effettuare (mput o mget) oppure Ctrl+D (UNIX)/Ctrl+Z (Windows) per terminare il client: ");
											continue;
										}
										try {
											inFile = new FileInputStream(f);
										}
										catch(FileNotFoundException e) {
											System.out.println("Errore di apertura dello stream di input sul file "+f.getName());
											e.printStackTrace();
											continue;
										}
										
										// trasmissione del nome
										
										//per trasmettere il nome invio anche il path assoluto del file, in modo che il server gestisca la creazione di
										//opportuni direttori dove collocare i files ricevuti
										outSock.writeUTF(f.getName());
										System.out.println("Inviato il nome del file " + f.getName());
										
										
										//ricezione esito da parte del server
										String esito;
										esito = inSock.readUTF();
										System.out.println("Esito trasmissione: " + esito);
										
										
										if(esito.contentEquals("salta file")) {
											System.out.println("Il file "+f.getName()+" è già presente sul direttorio del server. ");
											continue;
										}
										//se la risposta da parte del server è "attiva" posso trasferire il file
										else if(esito.contentEquals("attiva")) {
											System.out.println("Trasferimento della dimensione del file in corso: ");
											fileDim = f.length();
											outSock.writeUTF(String.valueOf(fileDim));
											System.out.println("Copia del file "+f.getName()+" sul server in corso");
											// trasferimento file
											
											//FileUtility.trasferisci_a_linee_UTF_e_stampa_a_video(new DataInputStream(inFile), outSock);
											FileUtility.trasferisci_a_byte_file_binario(new DataInputStream(inFile), outSock, fileDim);
											inFile.close(); 			// chiusura file
											System.out.println("Trasmissione di " + f.getName() + " terminata ");
											
											
										}
									}
								}
								else {
									System.out.println("Il percorso "+dirs[i]+" non individua una directory presente.");
									System.out.println("Inserire il tipo di richiesta che si vuole effettuare (mput o mget) oppure Ctrl+D (UNIX)/Ctrl+Z (Windows) per terminare il client: ");
									continue;
								}
							}
							
						}
						else {
							System.out.println("Inserire il tipo di richiesta che si vuole effettuare (mput o mget) oppure Ctrl+D (UNIX)/Ctrl+Z (Windows) per terminare il client: ");
							continue;
						}
					}
					
					else if(msgType.contentEquals("mget")) {
						//invio al server del tipo di comunicazione da effettuare
						
						outSock.writeUTF(msgType);
						
						
						
						//invio al server della lista di directories da richiedere
						System.out.println("Inserisci la lista delle directories che vuoi ottenere: ");
						if((dir=stdIn.readLine())!=null) {
							
							outSock.writeUTF(dir);
							
							
							//ricezione del numero di files da ricevere
							System.out.println("Ricezione del numero di directories da ricevere");
							try {
								countDir = Integer.parseInt(inSock.readUTF());
							}
							catch(NumberFormatException e) {
								System.out.println("Formattazione errata del numero di files da ricevere");
								e.printStackTrace();
								socket.close();
								System.out.println("Chiusura in corso.. ");
								System.exit(5);
							}
							System.out.println("Il client riceverà "+countDir+" directories");
							for(int i=0; i<countDir; i++) {
								dir = inSock.readUTF();
								countFiles = Integer.parseInt(inSock.readUTF());
								System.out.println("Leggo "+countFiles+" files dalla directory "+dir);
								for(int j = 0; j<countFiles; j++){
									
									fileName = inSock.readUTF();
									System.out.println("Ho ricevuto il nome del file: "+fileName);
									curFile = new File(fileName);
									if(curFile.exists()) {
										try {
											outSock.writeUTF("salta file");	
										}
										catch(SocketTimeoutException ste){
											System.out.println("Timeout scattato: ");
											ste.printStackTrace();
											System.out
												.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
											continue;          
										}        
										catch (Exception e) {
											System.err
												.println("\nProblemi durante la ricezione e scrittura del file: "
														+ e.getMessage());
											e.printStackTrace();
											continue;
										}
										System.out.println("File "+fileName+" già esistente");
										continue;
									}
									else {
										
											outSock.writeUTF("attiva");
											System.out.println("Ricezione della dimensione del file "+curFile.getName()+" in corso");
											fileDim = Integer.parseInt(inSock.readUTF());
											System.out.println("Il file "+curFile.getName()+" occuperà "+fileDim+ " bytes");
											curFile.createNewFile();
											outFile=new FileOutputStream(fileName);
											FileUtility.trasferisci_a_byte_file_binario(inSock, new DataOutputStream(outFile), fileDim);
											System.out.println("\nRicezione del file " + fileName
													+ " terminata\n");
										
										
										outFile.close();
									}
								}
								
							}
						}
						else {
							System.out.println("Inserire il tipo di richiesta che si vuole effettuare (mput o mget) oppure Ctrl+D (UNIX)/Ctrl+Z (Windows) per terminare il client: ");
							continue;
						}
						
					}
					else {
						System.out.print("Tipo di richiesta non riconosciuta! Inserire il tipo di richiesta che si vuole effettuare (mput o mget) oppure Ctrl+D (UNIX)/Ctrl+Z (Windows): ");
						continue;
					}
				}
				catch(SocketTimeoutException ste){
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					System.out.println("\nInserire il tipo di richiesta che si vuole effettuare (mput o mget) oppure Ctrl+D (UNIX)/Ctrl+Z (Windows) per terminare il client: ");

					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					continue;          
				}
				catch(IOException e){
					System.out
						.println("Errore di IO: ");
					e.printStackTrace();
					System.out.println("\nInserire il tipo di richiesta che si vuole effettuare (mput o mget) oppure Ctrl+D (UNIX)/Ctrl+Z (Windows) per terminare il client: ");

					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}
			}
			
			
		}
		catch(Exception e) {
			System.out.println("Errore irreversibile: "+e.getMessage());
			e.printStackTrace();
			System.out.println("Terminazione del client..");
			try {
				socket.close();
			} catch (IOException ioe) {
				System.out.println("Errore nella chiusura del client: ");
				ioe.printStackTrace();
				System.exit(6);
			}
			System.exit(4);
		}
		System.out.println("\nChiusura della connessione..");
		try {
			socket.close();
		} catch (IOException e) {
			System.out.println("Errore nella chiusura del client: ");
			e.printStackTrace();
			System.exit(6);
		}
	}
}
