import java.io.*;
import java.net.*;

public class SwapClient{

    public static void main(String[] args){
        InetAddress addr = null;
		int port = -1;
        String fileName = null;
		
		try {
			if (args.length == 3) {
		    addr = InetAddress.getByName(args[0]);
		    port = Integer.parseInt(args[1]);
            fileName = args[2];
			} else {
				System.out.println("Usage: java SwapClient serverIP serverPort fileName");
			    System.exit(1);
			}
		} catch (UnknownHostException e) {
			System.out
		      .println("Problemi nella determinazione dell'endpoint del server : ");
			e.printStackTrace();
			System.out.println("SwapClient: interrompo...");
			System.exit(2);
		}

		while(true){
			try{
				DatagramSocket socket = null;
				DatagramPacket packet = null;
				byte[] buf = new byte[256];

				// creazione della socket datagram, settaggio timeout di 30s
				// e creazione datagram packet
				try {
					socket = new DatagramSocket();
					socket.setSoTimeout(30000);
					packet = new DatagramPacket(buf, buf.length, addr, port);
					System.out.println("\nLineClient: avviato");
					System.out.println("Creata la socket: " + socket);
				} catch (SocketException e) {
					System.out.println("Problemi nella creazione della socket: ");
					e.printStackTrace();
					System.out.println("SwapClient: interrompo...");
					System.exit(1);
				}

				ByteArrayOutputStream boStream = null;
				DataOutputStream doStream = null;
				byte[] data = null;
				String nomeFile = null;
				int numLinea = -1;
				String richiesta = null;
				String risposta = null;
				ByteArrayInputStream biStream = null;
				DataInputStream diStream = null;
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

				int riga1 = -1;
				int riga2 = -1;
				int portaSwapRowServer = -1;

				/*
				* RICHIESTA
				*/
				// Mandare richiesta al DiscoveryServer con il nome del file
				try {
					richiesta = fileName;
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(richiesta);
					data = boStream.toByteArray();
					packet.setData(data);
					socket.send(packet);
					System.out.println("Richiesta inviata a " + addr + ", " + port);
				} catch (IOException e) {
					System.out.println("Problemi nell'invio della richiesta: ");
					e.printStackTrace();
					System.out
					.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}

				/*
				* RECEIVE DELLA RESPONSE
				*/
				// Ricezione del numero di porta del SwapRowServer al quale mandare la richiesta
				try {
					// settaggio del buffer di ricezione
					packet.setData(buf);
					socket.receive(packet);
					// sospensiva solo per i millisecondi indicati, dopodich� solleva una
					// SocketException
				} catch (IOException e) {
					System.out.println("Problemi nella ricezione del datagramma: ");
					e.printStackTrace();
					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}
				/*
				* PARSING DELLA RISPOSTA
				*/
				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					risposta = diStream.readUTF();
					System.out.println("Numero di porta di SwapRowServer: " + risposta);
					portaSwapRowServer = Integer.parseInt(risposta);
				} catch (IOException e) {
					System.out.println("Problemi nella lettura della risposta: ");
					e.printStackTrace();
					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}

				// Decisione delle righe da swappare
				// interazione con l'utente
				try {
					System.out.print("Quali linee vuoi scambiare?");
					String line = stdIn.readLine();
					riga1 = Integer.parseInt(line.split(" ")[0]);
					riga2 = Integer.parseInt(line.split(" ")[1]);
					//richiesta = riga1 + " " + riga2;
				} catch (Exception e) {
					System.out.println("Problemi nell'interazione da console: ");
					e.printStackTrace();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
					continue;
				}

				// Invio della richiesta con il numero di porta appena ottenuto
				try {
					richiesta = riga1 + " " + riga2;
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(richiesta);
					data = boStream.toByteArray();
					packet = new DatagramPacket(buf, buf.length, addr, portaSwapRowServer);
					packet.setData(data);
					socket.send(packet);
					System.out.println("Richiesta inviata a " + addr + ", " + portaSwapRowServer);
				} catch (IOException e) {
					System.out.println("Problemi nell'invio della richiesta: ");
					e.printStackTrace();
					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}

				/*
				 * MANCA RECEIVE PER IL RISULTATO DELL'OPERAZIONE
				 */
			}
			// qui catturo le eccezioni non catturate all'interno del while
			// in seguito alle quali il server termina l'esecuzione
			catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
}