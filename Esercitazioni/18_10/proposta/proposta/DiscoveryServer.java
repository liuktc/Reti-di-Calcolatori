import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class DiscoveryServer{

    public static void main(String[] args){
        System.out.println("DiscoveryServer avviato");

        DatagramSocket socket = null;
        DatagramPacket packet = null;
        byte[] buf = new byte[256];
        int port = -1;
        String[] nomiFile = null;
        int[] porteFile = null;

        //controllo argomenti
        if(args.length <= 2){
            System.out.println("Usage: java DiscoveryServer porta nomeFile1 port1 nomeFile2 port2");
            System.exit(1);
        }else if(args.length % 2 == 0){
            System.out.println("Usage: java DiscoveryServer porta nomeFile1 port1 nomeFile2 port2");
            System.exit(1);
        }
        try{
            port = Integer.parseInt(args[0]);
            // controllo che la porta sia nel range consentito 1024-65535
			if (port < 1024 || port > 65535) {
				System.out.println("Usage: java LineServer [serverPort>1024]");
				System.exit(1);
			}
        }catch(NumberFormatException e){
            System.out.println("Usage: java DiscoveryServer porta nomeFile1 port1 nomeFile2 port2");
            System.exit(1);
        }
        nomiFile = new String[(args.length-1)/2];
        porteFile = new int[(args.length-1)/2];
        for(int i=1;i<args.length;i+=2){
            nomiFile[i/2] = args[i];
            try{
                porteFile[i/2] = Integer.parseInt(args[i+1]);
                // controllo che la porta sia nel range consentito 1024-65535
                if (port < 1024 || port > 65535) {
                    System.out.println("Usage: java LineServer [serverPort>1024]");
                    System.exit(1);
                }
            }catch(NumberFormatException e){
                System.out.println("Usage: java DiscoveryServer porta nomeFile1 port1 nomeFile2 port2");
                System.exit(1);
            }
        }

        // Controllo che non ci siano porte o file ripetuti
        for(int i=0;i<porteFile.length;i++){
            for(int j=0;j<porteFile.length;j++){
                if((i != j && porteFile[i] == porteFile[j]) || (i != j &&  nomiFile[i] == nomiFile[j])){
                    //Errore porta o file ripetuti
                    System.out.println("Errore, impossibile avere due porte o due file uguali");
                    System.exit(1);
                }
            }
        }

        // Avvio i thread degli SwapRowServer
        SwapRowServer swapServers = null;
        for(int i=0;i<porteFile.length;i++){
            swapServers = new SwapRowServer(porteFile[i],nomiFile[i]);
            swapServers.start();
        }

        try {
			socket = new DatagramSocket(port);
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("Creata la socket: " + socket);
		}
        catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.exit(1);
		}

        try{
            String richiesta = null;
            int portaRichiesta = -1;
            String nomeFile = null;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			StringTokenizer st = null;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			String linea = null;
			byte[] data = null;

            //andiamo in loop infinito, il server è un demone che esegue sempre in attesa di richieste
            while(true){
                System.out.println("\n[DiscoveryServer] In attesa di richieste...");
				
				// ricezione del datagramma
				try {
					packet.setData(buf);
					socket.receive(packet);
				}
				catch (IOException e) {
					System.err.println("Problemi nella ricezione del datagramma: "
							+ e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}

                // Interpretazione del dato ricevuto
                try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					richiesta = diStream.readUTF();
					st = new StringTokenizer(richiesta);
					nomeFile = st.nextToken();
					System.out.println("[DiscoveryServer] Richiesta della porta del server che gestisce il file " + nomeFile);
				}
				catch (Exception e) {
					System.err.println("Problemi nella lettura della richiesta");
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}

                // Ricerca del server dedicato alla gestione del file ricevuto come input
                try {
                    //System.out.println("File disponibili:");
					for(int i=0;i<nomiFile.length;i++){
                        //System.out.println("- " + nomiFile[i]);
                        if(nomiFile[i].equals(nomeFile)){
                            portaRichiesta = porteFile[i];
                        }
                    }
                    if(portaRichiesta < 0){
                        System.out.println("[DiscoveryServer] Errore: il file specificato non esiste");
                        continue;
                    }
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
                    // Scrittura della porta richiesta nel datagram packet
					doStream.writeUTF(String.valueOf(portaRichiesta));
					data = boStream.toByteArray();
					packet.setData(data, 0, data.length);
					socket.send(packet);
				}
				catch (IOException e) {
					System.err.println("Problemi nell'invio della risposta: "
				      + e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}
            }
        }
        // qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
		}
    }
}