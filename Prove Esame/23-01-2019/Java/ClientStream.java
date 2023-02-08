/* Nome Cognome Matricola */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class ClientStream {
    public static void main(String[] args) throws IOException {
        InetAddress hostServerStream = null;
        int portServerStream = -1;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String req;
        Socket ssocket=null;
        DataInputStream inSock= null;
        DataOutputStream outSock=null;
        try {
            if (args.length == 2) {
                hostServerStream = InetAddress.getByName(args[0]);
                portServerStream = Integer.parseInt(args[1]);
                // controllo che la porta sia nel range consentito 1024-65535
				if (portServerStream < 1024 || portServerStream > 65535) {
					System.out.println("Usage: java Client hostServerStream portServerStream");
					System.exit(1);
				}
            } else {
                System.out
                        .println("Usage: java Client hostServerStream portServerStream");
                System.exit(1);
            }
        } // try
          // Per esercizio si possono dividere le diverse eccezioni
        catch (Exception e) {
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
            System.out
                    .println("Usage: java Client hostServerStream portServerStream");
            System.exit(2);
        }
        System.out.println("Esecuzione del servizio, ctrl+c per terminare");

          // creazione socket
          try {
            ssocket = new Socket(hostServerStream, portServerStream);
            // setto il timeout per non bloccare indefinitivamente il client
            ssocket.setSoTimeout(30000);
            System.out.println("Creata la socket: " + ssocket);
            inSock = new DataInputStream(ssocket.getInputStream());
            outSock = new DataOutputStream(ssocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("Problemi nella creazione degli stream su socket: ");
            ioe.printStackTrace();
            System.exit(-1);
        } 

        System.out.print("Premi E per l'eliminazione di una prenotazione, D per il download delle foto di una prenotazione (ctrl+z exit): ");
        while ((req = br.readLine()) != null) {
            if (!req.equals("E") && !req.equals("D")) {
                System.out.println("Errore di sintassi");
                System.out.print("Premi E per l'eliminazione di una prenotazione, D per il download delle foto di una prenotazione (ctrl+z exit): ");
                continue;
            }
                 
            /* Servizio di eliminazione */
            if (req.equals("E")) {
                // utilizzo socket
                try {
                    String ris, targa;
                    //leggo la targa
                    System.out.println("Inserire il numero di targa (5 caratteri): ");
                    targa=br.readLine();
                    if(targa.length()!=5){
                        System.out.println("La targa non ha cinque caratteri ");
                    }
                    // invio il tipo del servizio
                    outSock.writeUTF(req);
                    //invio la targa
                    outSock.writeUTF(targa);
                    
                    // leggo il risultato
                    ris = inSock.readUTF();
                    System.out.println(ris);
                    
                } catch (IOException ioe) {
                    System.out.println("Problemi nella creazione degli stream su socket: ");
                    ioe.printStackTrace();
                    System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
                    // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                    continue;
                } catch (Exception e) {
                    System.out.println("Problemi nella creazione della socket: ");
                    e.printStackTrace();
                    System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire solo invio per continuare: ");
                    // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                    continue;
                }
                System.out.println("\n\n---FINE ELIMINAZIONE---\n\n");
                
            } 
            /* Servizio di download di foto */
            else {
                System.out.print("Inserisci il nome della targa della prenotazione: ");
                String targa = br.readLine();
                int nread, num_foto;
                long dim;
                DataOutputStream ds;
                byte[] buffer = new byte[4096];
                //int risposta;
                // utilizzo socket
                try {
                    // invio il tipo del servizio
                    outSock.writeUTF(req);
                    // invio la targa
                    outSock.writeUTF(targa);
                    
                    
                    // leggo la risposta
                    num_foto=inSock.readInt();
                    for(int i=0; i<num_foto; i++){
                        ds=new DataOutputStream(new FileOutputStream(inSock.readUTF()));
                        dim=inSock.readLong();
                        while((nread=inSock.read(buffer, 0, (int)Math.min(buffer.length, dim)))>0){
                            ds.write(buffer, 0, nread);
                            dim=dim-nread;
                            if(dim==0){
                                ds.close();
                                break;
                            }
                        }
                    }   
                
                    

                } catch (IOException ioe) {
                    System.out.println("Problemi nella creazione degli stream su socket: ");
                    ioe.printStackTrace();
                    System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
                    // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                    continue;
                } catch (Exception e) {
                    System.out.println("Problemi nella creazione della socket: ");
                    e.printStackTrace();
                    System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
                    // il client continua l'esecuzione riprendendo dall'inizio del ciclo
                    continue;
                }
               
            } // else
            System.out.print("Premi E per l'eliminazione di una prenotazione, D per il download delle foto di una prenotazione (ctrl+z exit): ");
        } // while

        ssocket.shutdownInput();
        ssocket.shutdownOutput();
        ssocket.close();
    }// main
}// class Client