import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerStreamThread extends Thread{
    private Socket clientSocket = null;
    // Opzionalmente, anche questo potrebbe diventare un parametro (opzionale)!
    private int buffer_size = 4096;

    public ServerStreamThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run(){
        System.out.println("Attivazione figlio: " + Thread.currentThread().getName());

        DataOutputStream outSock;

        byte[] buffer = new byte[buffer_size];

        try {
            outSock = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("Problemi nella creazione degli stream di input/output su socket: ");
            ioe.printStackTrace();
            return;
        }

        try{
            String res = ServerStream.visualizzaStato();

            outSock.writeUTF(res);
        }


    }

}
