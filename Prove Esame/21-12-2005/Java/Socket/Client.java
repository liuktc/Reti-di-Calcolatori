import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException{
        InetAddress addrStream = null;
        InetAddress addrDatagram = null;
        int portStream = -1;
        int portDatagram = -1;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        Socket socket;
        DatagramSocket datagramSocket;
        DatagramPacket packet;
        DataInputStream inSock;
        DataOutputStream outSock;
        String res;
        byte[] buf = new byte[256];

        try{
            if(args.length == 4){
                addrStream = InetAddress.getByName(args[0]);
                portStream = Integer.parseInt(args[1]);
                addrDatagram = InetAddress.getByName(args[2]);
                portDatagram = Integer.parseInt(args[3]);

                if (portDatagram < 1024 || portDatagram > 65535 || portStream < 1024 || portStream > 65535) {
                    System.out.println(
                            "Usage: java Client hostServerStream portServerStream hostServerDatagram portServerDatagram");
                    System.exit(1);
                }
            }
        }catch(Exception e){
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
            System.out.println("Usage: java Client hostServerStream portServerStream hostServerDatagram portServerDatagram");
            System.exit(2);
        }

        String inputUtente;
        System.out.println("Scegliere il servizio (1:Visualizzazione stato,2:Sospensione stanza):");
        while((inputUtente = stdIn.readLine()) != null){
            if(inputUtente.equals("1")){
                // PARTE STREAM
                try {
                    socket = new Socket(addrStream, portStream);
                    socket.setSoTimeout(30000);
                    System.out.println("Creata la socket: " + socket);
                    inSock = new DataInputStream(socket.getInputStream());
                } catch (IOException ioe) {
                    System.out.println("Problemi nella creazione degli stream su socket: ");
                    ioe.printStackTrace();
                    System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
                    System.exit(1);
                }   

                try {
                    res = inSock.readUTF();
                    System.out.println(res); 
                } catch (IOException e) {
                    System.out.println("Problemi nella ricezione del messaggio: ");
                    e.printStackTrace();
                    System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, solo invio per continuare: ");
                    System.exit(1);
                }
                
            }else if(inputUtente.equals("2")){
                // PARTE DATAGRAM
                System.out.println("Inserisci il nome della stanza:");
                String nomeStanza = stdIn.readLine();

                datagramSocket = new DatagramSocket();
                datagramSocket.setSoTimeout(30000);
                packet = new DatagramPacket(buf, buf.length, addrDatagram, portDatagram);

                // Conversione del messaggio in array di byte
                ByteArrayOutputStream boStream = new ByteArrayOutputStream();
                DataOutputStream doStream = new DataOutputStream(boStream);
                doStream.writeUTF(nomeStanza); // Anche doStream.writeInt per scrittura tipizzata
                byte[] data = boStream.toByteArray();

                // Invio del messaggio sulla socket
                packet.setData(data);
                datagramSocket.send(packet);

                // Ricezione della risposta  del server
                packet.setData(buf);
                datagramSocket.receive(packet); // catch SocketTimeoutException

                ByteArrayInputStream biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                DataInputStream diStream = new DataInputStream(biStream);
                int risposta = diStream.readInt();

                if(risposta == 0){

                }else{

                }
            }else{
                System.out.println("Servizio non riconosciuto!!!");
            }
            System.out.println("Scegliere il servizio (1:Visualizzazione stato,2:Sospensione stanza):");
        }

    }
}
