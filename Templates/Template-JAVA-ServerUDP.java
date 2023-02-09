import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class LineServer {

	private static final int PORT = 4445;

	public static void main(String[] args) {

		System.out.println("Server: avviato");

		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];
		int port = -1;

		// controllo argomenti input: 0 oppure 1 argomento (porta)
		if ((args.length == 0)) {
			port = PORT;
		} else if (args.length == 1) {
			try {
				port = Integer.parseInt(args[0]);
				/* Controllo che la porta sia nel range consentito 1024-65535 */
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java Server [serverPort>1024]");
					System.exit(1);
				}
			} catch (NumberFormatException e) {
				System.out.println("Usage: java Server [serverPort>1024]");
				System.exit(1);
			}
		} else {
			System.out.println("Usage: java Server [serverPort>1024]");
			System.exit(1);
		}

        /* Creazione della socket datagram */
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

		try {
			String nomeFile = null;
			int numLinea = -1;
			String richiesta = null;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			StringTokenizer st = null;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			String result = null;
			byte[] data = null;

			while (true) {
				System.out.println("\nIn attesa di richieste...");
				
				/* Ricezione del datagramma */
				try {
					packet.setData(buf);
					socket.receive(packet);
				}
				catch (IOException e) {
					System.err.println("Problemi nella ricezione del datagramma: "
							+ e.getMessage());
					e.printStackTrace();
					continue;
				}

                /* Conversione del messaggio da array di byte a stringa */
				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					richiesta = diStream.readUTF();
                    /* Parsing della richiesta */
					st = new StringTokenizer(richiesta);
					nomeFile = st.nextToken();
					numLinea = Integer.parseInt(st.nextToken());
					System.out.println("Richiesta linea " + numLinea + " del file " + nomeFile);
				}
				catch (Exception e) {
					System.err.println("Problemi nella lettura della richiesta: "
						+ nomeFile + " " + numLinea);
					e.printStackTrace();
					continue;
				}

				try {
					result = LineUtility.getLine(nomeFile, numLinea);
                    /* Conversione della risposta da stringa ad array di byte */
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(result);
					data = boStream.toByteArray();
					packet.setData(data, 0, data.length);
					socket.send(packet);
				}
				catch (IOException e) {
					System.err.println("Problemi nell'invio della risposta: "
				      + e.getMessage());
					e.printStackTrace();
					continue;
				}

			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Server: termino...");
		socket.close();
	}
}