import java.io.*;
import java.net.*;

public class PutFileClient {
    public static void main(String[] args) throws IOException {
        InetAddress addr = null;
        int port = -1;
        int NUMERO_BYTE_PER_MESSAGGIO = 0;

        // Controllo argomenti
        try{
            if(args.length == 3){
                addr = InetAddress.getByName(args[0]);
                port = Integer.parseInt(args[1]);
                NUMERO_BYTE_PER_MESSAGGIO = Integer.parseInt(args[2]);
            }else if(args.length == 2){ 
                addr = InetAddress.getByName(args[0]);
                port = Integer.parseInt(args[1]);
            } else{ 
                System.out.println("Usage: java PutFileClient address port");
                System.exit(1); 
            }
        }
        catch(Exception e){ 
			e.printStackTrace();
			System.exit(2);
        }
        int[] numeri_da_utilizzare = new int[100];
        for(int i=0;i<100;i++){
            numeri_da_utilizzare[i] = (int)java.lang.Math.pow(2, (i/10.0f) + 10);
        }
        // oggetti per comunicazione e lettura file
        Socket socket = null; 
        String esito;
        FileInputStream inFile = null;
        String nomeFile = "matlab.zip";
        DataInputStream inSock = null; 
        DataOutputStream outSock = null;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("\n^D(Unix)/^Z(Win)+invio .... Nome file?");
        int cont = 0;
        try{
            while (true){
                if(new File(nomeFile).exists()){
                    try{ // creazione socket
                        socket = new Socket(addr, port);
                        socket.setSoTimeout(30000);
                        inSock = new DataInputStream(socket.getInputStream());
                        outSock = new DataOutputStream(socket.getOutputStream());
                        System.out.println("Socket creata con successo");
                    } catch(Exception e){
                        e.printStackTrace();
                        continue;
                    }
                }
                else{
                    System.out.println("File non presente");
                    System.out.print("\n^D(Unix)/^Z(Win)..."); 
                    continue;
                }
                // Apertura file
                try{ 
                    inFile = new FileInputStream(nomeFile);
                }catch(FileNotFoundException e){
                    e.printStackTrace();
                }

                // Invio file
                try{
                    outSock.writeUTF(nomeFile);
                    FileUtility.trasferisci_a_byte_file_binario(new DataInputStream(inFile),outSock,false,numeri_da_utilizzare[cont]);
                    System.out.println("File traferito con successo");
                    inFile.close(); // chiusura della socket e del file
                    socket.shutdownOutput(); // chiudo in upstream, cioe' invio EOF
                } catch (SocketTimeoutException te) {
                    te.printStackTrace();
                    continue;
                } catch(Exception e){
                    e.printStackTrace();
                    continue;
                }
                
                // Ricezione esito
                try{
                    esito = inSock.readUTF();
                    System.out.println(esito);
                    socket.shutdownInput(); // chiudo la socket in downstream
                }catch (SocketTimeoutException te) {
                    te.printStackTrace();
                    continue;
                }catch(Exception e){
                    e.printStackTrace();
                    continue;
                }
                //System.out.print("\n^D(Unix)/^Z(Win)+invio .... Nome file?");
                System.out.println("Trasferito cont=" + cont);
                cont++;
            } // while
        } // try
        catch(Exception e){
            e.printStackTrace();
            System.exit(3);
        }
    } // main
} // class
