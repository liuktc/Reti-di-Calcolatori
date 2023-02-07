import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSeq {
	private static final int PORT=4445;
	public static void main(String[] args)  {
		int port=-1;
		/* controllo argomenti */
		try {
			if (args.length == 1) {
				port = Integer.parseInt(args[0]);
				// controllo che la porta sia nel range consentito 1024-65535
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java PutFileServerSeq or java PutFileServerSeq port");
					System.exit(1);
				}
			} else if (args.length == 0) {
				port = PORT;
			} else {
				System.out
					.println("Usage: java PutFileServerSeq or java PutFileServerSeq port");
				System.exit(1);
			}
		} //try
		catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out
				.println("Usage: java PutFileServerSeq or java PutFileServerSeq port");
			System.exit(1);
		}
		ServerSocket serverSocket=null;
		//creo una socket in ascolto sulla porta specificata
				
		try {
//			serverSocket = new ServerSocket(port,2);
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			System.out.println("PutFileServerSeq: avviato ");
			System.out.println("Creata la server socket: " + serverSocket);
		}
		catch (Exception e) {
			System.err.println("Problemi nella creazione della server socket: "
					+ e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}
		
		Socket clientSocket=null;
		DataInputStream inSock=null;
		DataOutputStream outSock=null;
		String msgType=null;
		File curFile=null;
		FileOutputStream outFile = null;
		String line=null, dir=null;
		String[] dirs=null;
		File d[] = null;
		FileInputStream inFile=null;
		int countDir=0, countFiles=0;
		long fileDim=0;
		
		//protocollo: il server riceve o un messaggio mget o mput
		//se riceve mget aspetta la lista delle directories che deve inviare, invia il numero totale di files da inviare e poi ritorna a chiedere il tipo di messaggio che si vuole inviare, fintantochè il cilent non si disconnetta
		//se riceve mput aspetta il numero di files che riceverà e poi i files stessi da ricevere. Quando il conteggio arriva a 0 aspetta che lo stesso client gli comunichi che tipo di messaggio vuole inviare fintantochè il client non si disconnetta
		try {
			while(true) {
				//creazione della socket da parte del client
				System.out.println("In attesa di richieste..");
				try {
					clientSocket=serverSocket.accept();
					clientSocket.setSoTimeout(30000);
					System.out.println("Connessione accettata: "+clientSocket);
				}
				catch(SocketTimeoutException ste) {
					System.out.println("Non ho ricevuto nulla dal client per 30 sec., interrompo la comunicazione e accetto nuove richieste.");
				}
				catch(IOException e) {
					System.out.println("Problemi nell'accettazione della connessione: "+e.getMessage());
					e.printStackTrace();
					System.exit(4);
				}
				
				//creazione degli stream di input e output sulla clientSocket e ricezione del tipo di comunicazione che si vuole effettuare

				try {
					inSock = new DataInputStream(clientSocket.getInputStream());
					outSock = new DataOutputStream(clientSocket.getOutputStream());
		        }
				catch(SocketTimeoutException ste){
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					clientSocket.close();
					System.out.println("Chiusura della comunicazione con il client");
					continue;          
				}
				
				catch (IOException e) {
		        	System.out
		        		.println("Problemi nella creazione degli stream di input/output "
		        			+ "su socket: ");
		        	e.printStackTrace();
		        	clientSocket.close();
		        	System.out.println("Terminazione della comunicazione con il client");
		        	// il server continua l'esecuzione riprendendo dall'inizio del ciclo
		        	continue;
		        }
				try {
					//il client continua ad inviare richieste finchè l'utente non decide di terminare il programma e quindi fintantochè il client rimane connesso
					while(clientSocket.isBound()) {
						//le eccezioni che possono essere generate da readUTF e writeUTF sono gestite all'esterno del ciclo
						msgType = inSock.readUTF();
						if(msgType.contentEquals("mget")) {
							//gestione della richiesta get
							
							//conto il numero di files che trasferirò
							countDir = 0;
							System.out.println("Ricezione delle directory da mandare al client: ");
							line = inSock.readUTF();
							dirs = line.split(" ");
							d = new File[dirs.length];
							for(int i = 0; i<dirs.length; i++){
								d[i] = new File(dirs[i]);
								if(d[i].isDirectory()) {
									countDir ++;
								}
							}
							outSock.writeUTF(String.valueOf(countDir));
							
							//trasferimento delle directories
							for(int i = 0; i<dirs.length; i++){
								if(d[i].isDirectory()) {
									System.out.println("Invio del nome della directory al client: ");
									outSock.writeUTF(d[i].getName());
									System.out.println("Invio al server il numero di files contenuti dentro la directory "+dirs[i]);
									outSock.writeUTF(String.valueOf(d[i].listFiles().length));
									System.out.println("Invio al client in corso della directory "+d[i].getCanonicalPath());
									for(File f: d[i].listFiles()) {
										
										try {
											inFile = new FileInputStream(f);
										}
										catch(FileNotFoundException e) {
											System.out.println("Errore di apertura dello stream di input sul file "+f.getName());
											e.printStackTrace();
											System.out.println("Chiusura della comunicazione con il client");
											clientSocket.close();
											continue;
										}
										
										// trasmissione del nome
										
										//per trasmettere il nome invio anche il path assoluto del file, in modo che il server gestisca la creazione di
										//opportuni direttori dove collocare i files ricevuti
										outSock.writeUTF("./"+d[i].getName()+"/"+ f.getName());
										System.out.println("Inviato al client il nome del file " + f.getName());
										
										
										//ricezione esito da parte del server
										String esito;
										
										esito = inSock.readUTF();
										System.out.println("Esito trasmissione: " + esito);
										
										
										
										if(esito.contentEquals("salta file")) {
											System.out.println("Il file "+f.getName()+" è già presente sul direttorio del client. ");
											continue;
										}
										//se la risposta da parte del server è "attiva" posso trasferire il file
										else if(esito.contentEquals("attiva")) {
											System.out.println("Copia del file "+f.getName()+" sul client in corso");
											fileDim = curFile.length();
											outSock.writeUTF(String.valueOf(fileDim));
											// trasferimento file
											
											//FileUtility.trasferisci_a_linee_UTF_e_stampa_a_video(new DataInputStream(inFile), outSock);
											FileUtility.trasferisci_a_byte_file_binario(new DataInputStream(inFile), outSock, fileDim);
											inFile.close(); 			// chiusura file
											System.out.println("Trasmissione di " + f.getName() + " terminata ");
											
										}
									}
								}
								else {
									System.out.println("Il percorso "+d[i].getPath()+" non individua una directory presente.");
									
									continue;
								}
							}
						}
						else if(msgType.contentEquals("mput")) {
							//gestione della richiesta put
							
							System.out.println("Ricezione del numero di files da passare: ");
							try {
								countDir = Integer.parseInt(inSock.readUTF());
							}
							catch(NumberFormatException e) {
								System.out.println("Formattazione del numero di files da trasferire errata");
								clientSocket.close();
								System.out.println("Chiusura della clientSocket");
								continue;
							}
							System.out.println("Il server riceverà "+countDir+" directories");
							
							for(int i=0; i<countDir; i++) {
								//ricezione del numero di files
								System.out.println("Leggo il nome della directory da ricevere: ");
								dir = inSock.readUTF();
								System.out.println("Leggo i files della directory "+dir+": ");
								if((new File(dir).mkdirs())||new File(dir).exists()) {
									countFiles = Integer.parseInt(inSock.readUTF());
									System.out.println("Nella directory "+dir+ " ci saranno "+countFiles+" files");
									for(int j = 0; j<countFiles; j++) {
										String nomeFile=null;
										System.out.println("Leggo il nome del prossimo file da ricevere: ");
										nomeFile = inSock.readUTF();
								       
										if(nomeFile==null) {
											System.out.println("Nome del file non passato");
											continue;
										}
										else {
											
											//per questioni di sicurezza sarebbe meglio non accettare qualsiasi nome di file 
											//passato dall'utente (potrebbe essere il path per un file di sistema!)
											curFile = new File("./"+dir+File.separator+nomeFile);
											//se il file è già presente sul server invio il messaggio "salta file"
											if(curFile.exists()) {
												System.out.println("File "+curFile.getName()+" già esistente");
												
												outSock.writeUTF("salta file");
												
											}
											//se il file non è ancora presente sul server invio il messaggio "attiva"
											else {
												System.out.println("Il file "+curFile.getName()+" non è ancora presente sul server. Gestione della richiesta put in corso");
												
												outSock.writeUTF("attiva");
												System.out.println("Ricezione della dimensione del file "+curFile.getName()+" in corso");
												fileDim = Integer.parseInt(inSock.readUTF());
												System.out.println("Il file "+curFile.getName()+" occuperà "+fileDim+ " bytes");
												curFile.createNewFile();
												outFile= new FileOutputStream(curFile);
												
												System.out.println("Ricevo il file " + nomeFile + ": \n");
												/**NOTA: la funzione consuma l'EOF*/
												FileUtility.trasferisci_a_byte_file_binario(inSock,
														new DataOutputStream(outFile), fileDim);
												System.out.println("\nRicezione del file " + nomeFile
														+ " terminata\n");
												outFile.close();				// chiusura file 
												
											}
										}
									}
								}
								else {
									System.out.println("Creazione della directory " +dir+" non riuscita");
									clientSocket.close();
									break;
								}
							}
			
						}
						else {
							System.out.println("Tipo di messaggio non riconosciuto");
							outSock.writeUTF("Tipo di messaggio non riconosciuto");
						}
						
						
					}
				}
				catch(SocketTimeoutException ste) {
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					clientSocket.close();
					System.out.println("Chiusura della comunicazione con il client");
					continue;
				}
				catch(IOException e) {
					System.out.println("Errore nell'invio del messaggio: ");
					e.printStackTrace();
					clientSocket.close();
					System.out.println("Chiusura della comunicazione con il client");
					continue;
				}
			}
		}
		catch(Exception e) {
			System.out.println("Errore irreversibile");
			e.printStackTrace();
			try {
				System.out.println("Chiusura della socket lato server..");
				serverSocket.close();
			} catch (IOException e1) {
				System.out.println("Errore di chiusura della serverSocket");
				e1.printStackTrace();
			}
		}
	}
}
