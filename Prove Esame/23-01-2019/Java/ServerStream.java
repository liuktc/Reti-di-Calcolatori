import java.io.*;
import java.net.*;

class ServerStreamThread extends Thread {
    private Socket clientSocket = null;
    // Opzionalmente, anche questo potrebbe diventare un parametro (opzionale)!
    private int buffer_size = 4096;

    public ServerStreamThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        System.out.println("Attivazione figlio: " + Thread.currentThread().getName());

        DataInputStream inSock;
        DataOutputStream outSock;

        byte[] buffer = new byte[buffer_size];
        int cont = 0;
        int read_bytes = 0;
        DataOutputStream dest_stream = null;
        String ris, folder_name;
        File folder;
        File[] foto;
        DataInputStream bf;

        try {
            inSock = new DataInputStream(clientSocket.getInputStream());
            outSock = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("Problemi nella creazione degli stream di input/output su socket: ");
            ioe.printStackTrace();
            return;
        }
        try {
            try {
                String servizio;

                while ((servizio = inSock.readUTF()) != null) {
                    if(servizio.equals("E")){
                        String targa=inSock.readUTF();
                        ris=ServerStream.elimina_prenotazione(targa);
                        outSock.writeUTF(ris);

                    }else if(servizio.equals("D")){
                        String targa=inSock.readUTF();
                        if((folder_name=ServerStream.cerca_folder(targa))==null){
                            outSock.close();
                            inSock.close();
                            clientSocket.close();
                            System.exit(-1);
                        }
                        folder=new File(folder_name);
                        if(folder.exists() && folder.isDirectory()){
                            foto=folder.listFiles();
                            outSock.writeInt(foto.length-2);
                            for(int i=0; i<foto.length;i++){
                                if(foto[i].isFile()){
                                    outSock.writeUTF(foto[i].getName());
                                    outSock.writeLong(foto[i].length());
                                    bf=new DataInputStream(new FileInputStream(foto[i]));
                                    while((read_bytes=bf.read(buffer))>0){
                                        outSock.write(buffer, 0, read_bytes);
                                    }
                                }
                            }
                        }

                    }else{
                        System.out.println("Servizio non presente");
                    }
                                            
                } // while
            } catch (EOFException eof) {
                System.out.println("Raggiunta la fine delle ricezioni, chiudo...");
                clientSocket.close();
                System.out.println("PutFileServer: termino...");
                System.exit(0);
            } catch (SocketTimeoutException ste) {
                System.out.println("Timeout scattato: ");
                ste.printStackTrace();
                clientSocket.close();
                System.exit(1);
            } catch (Exception e) {
                System.out.println("Problemi, i seguenti : ");
                e.printStackTrace();
                System.out.println("Chiudo ed esco...");
                clientSocket.close();
                System.exit(2);
            }
        } catch (IOException ioe) {
            System.out.println("Problemi nella chiusura della socket: ");
            ioe.printStackTrace();
            System.out.println("Chiudo ed esco...");
            System.exit(3);
        }
    }

}// thread

public class ServerStream {
    private static final int N=10;
    static Prenotazione[] prenotazioni=null;


    public static synchronized String elimina_prenotazione(String targa){
        File folder;
        String folder_name;
        File[] foto;
        for(int i=0; i<N; i++){
            if(prenotazioni[i].getTarga().equals(targa)){
                prenotazioni[i].setPatente("0");
                prenotazioni[i].setTarga("L");
                prenotazioni[i].setTipo("L");
                folder=new File(prenotazioni[i].getFolder());
                if(folder.exists() && folder.isDirectory()){
                    foto=folder.listFiles();
                    for(int j=0; j<foto.length;j++){
                        if(foto[j].isFile()){
                            foto[j].delete();
                        }
                    }
                    return "prenotazione eliminata";
                }else{
                    return "Cartella non trovata";
                }

            }
        }
        return "Non trovata prenotazione";
    }

    public static String cerca_folder(String targa){
        for(int i=0; i<N; i++){
            if(prenotazioni[i].getTarga().equals(targa)){
                return prenotazioni[i].getFolder();
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        int port = -1;

        try {
            if (args.length == 1) {
                port = Integer.parseInt(args[0]);
                // controllo che la porta sia nel range consentito 1024-65535
                if (port < 1024 || port > 65535) {
                    System.out.println("Usage: java ServerStream [serverPort>1024]");
                    System.exit(1);
                }
            } else {
                System.out.println("Usage: java ServerStream port");
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
            System.out.println("Usage: java ServerStream port");
            System.exit(1);
        }

        prenotazioni=new Prenotazione[N];
        for(int i=0; i<N; i++){
            prenotazioni[i]=new Prenotazione();
        }

        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            System.out.println("ServerStream: avviato ");
            System.out.println("Server: creata la server socket: " + serverSocket);
        } catch (Exception e) {
            System.err.println("Server: problemi nella creazione della server socket: " + e.getMessage());
            e.printStackTrace();
            serverSocket.close();
            System.exit(1);
        }

        try {
            while (true) {
                System.out.println("Server: in attesa di richieste...\n");

                try {
                    clientSocket = serverSocket.accept(); // bloccante!!!
                    System.out.println("Server: connessione accettata: " + clientSocket);
                } catch (Exception e) {
                    System.err.println("Server: problemi nella accettazione della connessione: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }

                try {
                    new ServerStreamThread(clientSocket).start();
                } catch (Exception e) {
                    System.err.println("Server: problemi nel server thread: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            } // while true
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Server: termino...");
            System.exit(2);
        }
    }
}
