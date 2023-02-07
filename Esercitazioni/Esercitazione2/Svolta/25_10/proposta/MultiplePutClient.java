import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class MultiplePutClient {
    
    public static void main(String[] args){
        InetAddress addr = null;
		int port = -1;
        int soglia = -1;
		
        // Controllo argomenti
		try{
			if(args.length == 3){
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
                soglia = Integer.parseInt(args[2]);
			} else{
				System.out.println("Usage: java MultiplePutClient serverAddr serverPort soglia");
				System.exit(1);
			}
		}catch(Exception e){
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java MultiplePutClient serverAddr serverPort soglia");
			System.exit(2);
		}

        // Oggetti utilizzati dal client per la comunicazione e la lettura del file locale
		Socket socket = null;
		FileInputStream inFile = null;
		DataInputStream inSock = null;
		DataOutputStream outSock = null;
		String nomeCartella = null;

        // Creazione stream di input da tastiera
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        try{
            System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome direttorio: ");
            while((nomeCartella=stdIn.readLine()) != null){
                File cartella = new File(nomeCartella);
                if(cartella.exists() && cartella.isDirectory()){
                    // Possiamo iniziare la comunicazione con il server

                    // Creazione socket
					try{
						socket = new Socket(addr, port);
						socket.setSoTimeout(30000);
						System.out.println("Creata la socket: " + socket);
					}
					catch(Exception e){
						System.out.println("Problemi nella creazione della socket: ");
						e.printStackTrace();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome direttorio: ");
						continue;
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					}

                    // Creazione stream di input/output su socket
					try{
						inSock = new DataInputStream(socket.getInputStream());
						outSock = new DataOutputStream(socket.getOutputStream());
					}
					catch(IOException e){
						System.out.println("Problemi nella creazione degli stream su socket: ");
						e.printStackTrace();
						System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome direttorio: ");
						continue;
						// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					}

                    /*
                     * Per ogni file all'interno della cartella indicata comunichiamo al server il nome del file
                     * e in caso di esito 'attiva' allora inviamo dimensione del file e il suo contenuto
                     */
                    //for (File f : cartella.listFiles()) {
                    File[] files = cartella.listFiles();
                    DataInputStream dataInputStream = null;
                    for(int i=0;i<files.length;i++){
                        String nomeFile = files[i].getName();
                        if(files[i].length() >= soglia){
                            // Trasmissione del nome
                            try{
                                outSock.writeUTF(nomeFile);
                                System.out.println("Inviato il nome del file " + nomeFile);
                            }
                            catch(Exception e){
                                System.out.println("Problemi nell'invio del nome di " + nomeFile + ": ");
                                e.printStackTrace();
                                System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome direttorio: ");
                                // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                                continue;
                            }


                            try{
                                String risposta = inSock.readUTF();
                                if(risposta.equals("attiva")){
                                    System.out.println("Possiamo inviare il file " + nomeFile);
                                    // Invio della dimensione del file
                                    /*  ---------------------------------------------------------------------------------------
                                    *  ---------------------------------------------------------------------------------------
                                    * ATTENZIONE: BRUTTO CAST A INT, SE IL FILE E' DI GRANDI DIMENSIONI NON FUNZIONA, DA SISTEMARE
                                    *  ---------------------------------------------------------------------------------------
                                    *  ---------------------------------------------------------------------------------------
                                    */
                                    outSock.writeInt((int)files[i].length());
                                    
                                    dataInputStream = new DataInputStream(new FileInputStream(files[i]));

                                    outSock.write(dataInputStream.readAllBytes());

                                    System.out.println("File " + nomeFile + " inviato correttamente");

                                    dataInputStream.close();
                                }else if(risposta.equals("salta")){
                                    System.out.println("Il file " + nomeFile + " esiste già, non verrà inviato");
                                }else{
                                    System.out.println("Messaggio sconosciuto da parte del server");
                                }
                            }catch(IOException e){
                                System.out.println("Errore nell'invio del file al server:");
                                e.printStackTrace();
                            }
                        }else{
                            System.out.println("Il file " + nomeFile + " non supera la dimensione di soglia");
                        }
                    }
                    socket.close();
                }else{
                    System.out.println("Il direttorio specificato non esiste o non è un direttorio");
                    continue;
                }
                
                System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti nome direttorio: ");
            }
        }catch(Exception e){
            System.out.println("Errore generico:");
            e.printStackTrace();
        }
    }
}
