//package proposta;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class MultiplePutServer {
    
    public static final int PORT = 54321; // porta default per server
	public static final int NUMERO_BYTE_PER_MESSAGGIO = 1024;
    public static void main(String[] args){
        int port = -1;

        // Controllo argomenti
        try {
			if (args.length == 1) {
				port = Integer.parseInt(args[0]);
				// controllo che la porta sia nel range consentito 1024-65535
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java MultiplePutServer or java MultiplePutServer port");
					System.exit(1);
				}
			} else if (args.length == 0) {
				port = PORT;
			} else {
				System.out.println("Usage: java MultiplePutServer or java MultiplePutServer port");
				System.exit(1);
			}
		} //try
		catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out.println("Usage: java MultiplePutServer or java MultiplePutServer port");
			System.exit(1);
		}

        /* preparazione socket e in/out stream */
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			System.out.println("MultiplePutServer: avviato ");
			System.out.println("Creata la server socket: " + serverSocket);
		}
		catch (Exception e) {
			System.err.println("Problemi nella creazione della server socket: "
					+ e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}

        try{
            //Ciclo infinito del server
            /*
             * 1) LISTEN E ACCETTAZIONE DI CONNESSIONE SULLA SOCKET
             * LOOP:
             *      2) RICEZIONE DEL NOME DEL FILE DAL CLIENT
             *      3) INVIO DELLA RISPOSTA 'attiva' O 'salta'
             *          3.A) RICEZIONE DELLA LUNGHEZZA DEL FILE IN ARRIVO
             *          3.B) RICEZIONE DEL FILE DA PARTE DEL CLIENT
             */
            while(true){
                Socket clientSocket = null;
				DataInputStream inSock = null;
				DataOutputStream outSock = null;
				DataOutputStream outFile = null;

                /* ----------------------------------------------------------------------------
                 * 1) LISTEN E ACCETTAZIONE DI CONNESSIONE SULLA SOCKET
                 */
                System.out.println("\nIn attesa di richieste...");
				try {
					clientSocket = serverSocket.accept();
					clientSocket.setSoTimeout(30000); //timeout altrimenti server sequenziale si sospende
					System.out.println("Connessione accettata: " + clientSocket + "\n");
				}
				catch (SocketTimeoutException te) {
					System.err
						.println("Non ho ricevuto nulla dal client per 30 sec., interrompo "
								+ "la comunicazione e accetto nuove richieste.");
					// il server continua a fornire il servizio ricominciando dall'inizio
					continue;
				}
				catch (Exception e) {
					System.err.println("Problemi nella accettazione della connessione: "
							+ e.getMessage());
					e.printStackTrace();
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo, se ci sono stati problemi
					continue;
				}
                //------------------------------------------------------------------------------

                /*
                 * CREAZIONE DELLE SOCKET DI INPUT E DI OUTPUT
                 */

				String nomeFile = null;
				try {
					inSock = new DataInputStream(clientSocket.getInputStream());
					outSock = new DataOutputStream(clientSocket.getOutputStream());
		        }
				catch(SocketTimeoutException ste){
					System.out.println("Timeout scattato: ");
					ste.printStackTrace();
					clientSocket.close();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
					continue;          
				}
				catch (IOException e) {
		        	System.out
		        		.println("Problemi nella creazione degli stream di input/output "
		        			+ "su socket: ");
		        	e.printStackTrace();
		        	// il server continua l'esecuzione riprendendo dall'inizio del ciclo
		        	continue;
		        }
                
                // Usiamo EOFException per capire quando lo stream è stato chiuso lato cliente
                // quindi faremo un loop infinito che si interromperà solamente andando nel catch EOFException
                try{
                    while(true){
                        // Leggo da input stream il nome del file
                        nomeFile = inSock.readUTF();
                        
                        if (nomeFile == null) {
                            System.out.println("Problemi nella ricezione del nome del file: ");
                            clientSocket.close();
                            break;
                        }

                        File file = new File(nomeFile);
                        long lunghezzaFile = -1;
                        
                        if(file.exists()){
                            // IL FILE GIA ESISTE, DOBBIAMO DIRE AL CLIENT DI NON INVIARE IL FILE
                            outSock.writeUTF("salta");
                        }else{
                            // IL FILE NON ESISTE, DOBBIAMO DIRE AL CLIENT CHE PUO' INVIARE IL FILE
                            outSock.writeUTF("attiva");
                            /*
                            * 3.A) RICEZIONE DELLA LUNGHEZZA DEL FILE IN ARRIVO
                            */
                            lunghezzaFile = inSock.readLong();
                            /*
                             * 3.B) RICEZIONE DEL FILE INVIATO DAL CLIENT
                             */
                            //data = inSock.readNBytes(lunghezzaFile);
							// Scrittura sul disco locale del file letto
							outFile = new DataOutputStream(new FileOutputStream(file));
							FileUtility.trasferisci_file_binario_n_byte(inSock, outFile,lunghezzaFile, NUMERO_BYTE_PER_MESSAGGIO);
							outFile.close();
                           
                            System.out.println("File " + nomeFile + " scritto con successo su disco");
                        }
                    }
                }catch(EOFException eof){
                    System.out.println("Lo stream è stato chiuso dal client");
					if(outFile != null)	outFile.close();
                    continue;
                }catch(IOException e){
                    System.out.println("Errore nella ricezione dei messaggi dal client");
					if(outFile != null)	outFile.close();
                    e.printStackTrace();
                    continue;
                }
                System.out.println("Il server ha chiuso la socket");
                clientSocket.close();
            }
        }catch (Exception e) {
			e.printStackTrace();
			// chiusura di stream e socket
			System.out.println("Errore irreversibile, MultiplePutServer: termino...");
			System.exit(3);
		}
    }
}
