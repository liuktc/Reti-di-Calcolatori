import java.io.*;
import java.net.*;
import java.time.*;
import java.time.temporal.TemporalUnit;

public class PutFileServerSeqcopy {
    public static final int PORT = 54321; // porta di default
    public static void main(String[] args) throws IOException{
        int port = -1;
        String nomeFile;
        FileOutputStream outFile = null;
        String esito;
        int NUMERO_BYTE_PER_MESSAGGIO = 0;
        try{
            if(args.length == 2){
                port = Integer.parseInt(args[0]);
                NUMERO_BYTE_PER_MESSAGGIO = Integer.parseInt(args[1]);
            }
            if (args.length == 1) {
                port = Integer.parseInt(args[0]);
            } else if (args.length == 0) {
                port = PORT;
            } else { 
                System.out.println("Usage: java PutFileServerSeq port");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        int[] numeri_da_utilizzare = new int[100];
        for(int i=0;i<100;i++){
            numeri_da_utilizzare[i] = (int)java.lang.Math.pow(2, (i/5.0f) + 10);
        }

        // Preparazione socket e in/out stream
        ServerSocket serverSocket = null; 
        try{
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
        }catch (Exception e) {
            e.printStackTrace();
        }

        try{ 
            while (true){// ciclo infinito del server
                System.out.println("In attesa di richiesta di connessione sulla porta " + port);
                Socket clientSocket = null;
                DataInputStream inSock = null; DataOutputStream outSock = null;
                try{
                    clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(30000);
                }catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                // creazione stream di I/O
                try {
                    inSock =new DataInputStream(clientSocket.getInputStream());
                    outSock =new DataOutputStream(clientSocket.getOutputStream());
                    nomeFile = inSock.readUTF();
                    System.out.println("In arrivo il file '" + nomeFile + "'");
                }catch (SocketTimeoutException te) {
                    te.printStackTrace();
                    continue;
                }catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                // ricezione file su file nuovo
                if (nomeFile == null) { 
                    clientSocket.close();
                    continue;
                }else {
                    File curFile = new File(nomeFile);
                    if (curFile.exists()) {
                        try{
                            esito = "File sovrascritto";
                            curFile.delete(); // distruggo il file
                        }catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                    } else esito = "Creato nuovo file";
                    outFile = new FileOutputStream(nomeFile);
                }
                LocalTime initialTime = LocalTime.now(); 
                BufferedWriter out = new BufferedWriter(new FileWriter("data.csv", true));
                // ricezione file
                try {
                    // N.B. la funzione consuma l’EOF
                    FileUtility.trasferisci_a_byte_file_binario(inSock, new DataOutputStream(outFile),true,NUMERO_BYTE_PER_MESSAGGIO);
                    System.out.println("File ricevuto correttamente in " + Duration.between(initialTime, LocalTime.now()).getSeconds() + " secondi");
                    out.append(NUMERO_BYTE_PER_MESSAGGIO + "," + (Duration.between(initialTime, LocalTime.now()).toMillis()) + "\n");
                    out.close();
                    outFile.close(); // chiusura file
                    clientSocket.shutdownInput();
                    outSock.writeUTF(esito+", file salvato su server");
                    clientSocket.shutdownOutput();
                }catch (SocketTimeoutException te) { 
                    te.printStackTrace();
                    continue;
                }catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }catch (Exception e) {
            System.exit(3);
        }
    }
}