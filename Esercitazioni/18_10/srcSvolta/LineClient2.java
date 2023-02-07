import java.io.*;
import java.net.*;

public class LineClient2 {

	public static void main(String[] args) {

		InetAddress addr = null;
		int port = -1;
		String nome = null;
		
		try {
			if(args.length == 3){
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
				nome = args[2];
			}else if (args.length == 2) {
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
			} else {
				System.out.println("Usage: java LineClient serverIP serverPort");
			    System.exit(1);
			}
		} catch (UnknownHostException e) {
			System.out
		      .println("Problemi nella determinazione dell'endpoint del server : ");
			e.printStackTrace();
			System.out.println("LineClient: interrompo...");
			System.exit(2);
		}
	
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
			System.out.println("LineClient: interrompo...");
			System.exit(1);
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		try {
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			byte[] data = null;
			String richiesta = null;
			String risposta = null;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			
			/*
			 * 1) CREAZIONE DATAGRAMMA
			 * 2) INVIO DEL PACCHETTO
			 * 3) ATTESA RICEZIONE RISPOSTA
			 * 4) PARSING DELLA RISPOSTA RICEVUTA
			 */
			while (true) {
				
				/*
				 * 1) CREAZIONE DATAGRAMMA
				 */
				try {
					System.out.print(nome + ": ");
					richiesta = nome + ": " + stdIn.readLine();
				} catch (Exception e) {
					System.out.println("Problemi nell'interazione da console: ");
					e.printStackTrace();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
					continue;
				}

				/*
				 * 2) INVIO DEL PACCHETTO
				 */
				try {
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(richiesta);
					data = boStream.toByteArray();
					packet.setData(data);
					socket.send(packet);
					//System.out.println("Messaggio inviato a " + addr + ", " + port);
				} catch (IOException e) {
					System.out.println("Problemi nell'invio della richiesta: ");
					e.printStackTrace();
					System.out
				      .print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}

				/*
				 * 3) ATTESA RICEZIONE RISPOSTA
				 */
				try {
					// settaggio del buffer di ricezione
					packet.setData(buf);
					socket.receive(packet);
					// sospensiva solo per i millisecondi indicati, dopodich� solleva una
					// SocketException
				} catch (IOException e) {
					System.out.println("Problemi nella ricezione del datagramma: ");
					e.printStackTrace();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}

				/*
				 * 4) PARSING DELLA RISPOSTA RICEVUTA
				 */
				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					risposta = diStream.readUTF();
					System.out.println(risposta);
				} catch (IOException e) {
					System.out.println("Problemi nella lettura della risposta: ");
					e.printStackTrace();
					System.out
				      .print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}
			}
		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il client termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("LineClient: termino...");
		socket.close();
	}
}