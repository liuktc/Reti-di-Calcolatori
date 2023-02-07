import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class LineServer2 {

	// porta nel range consentito 1024-65535!
	// dichiarata come statica perch� caratterizza il server
	private static final int PORT = 4445;

	public static void main(String[] args) {

		System.out.println("LineServer: avviato");

		DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];
		int port = -1;
		InetAddress address = null;
		String nome = null;

		// controllo argomenti input: 0 oppure 1 argomento (porta)
		if ((args.length == 0)) {
			port = PORT;
		} else if (args.length == 1) {
			try {
				port = Integer.parseInt(args[0]);
				// controllo che la porta sia nel range consentito 1024-65535
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java LineServer [serverPort>1024]");
					System.exit(1);
				}
			} catch (NumberFormatException e) {
				System.out.println("Usage: java LineServer [serverPort>1024]");
				System.exit(1);
			}
		} else if(args.length == 2){
			try{
				address = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
				// controllo che la porta sia nel range consentito 1024-65535
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java LineServer [serverPort>1024]");
					System.exit(1);
				}
			}catch(Exception e){
				e.printStackTrace();
				System.exit(1);
			}
		}else if(args.length == 3){
			try{
				address = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
				nome = args[2];
				// controllo che la porta sia nel range consentito 1024-65535
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java LineServer [serverPort>1024]");
					System.exit(1);
				}
			}catch(Exception e){
				e.printStackTrace();
				System.exit(1);
			}
		}else {
			System.out.println("Usage: java LineServer [serverPort>1024]");
			System.exit(1);
		}

		try {
			socket = new DatagramSocket(port);
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("Creata la socket: " + socket + " sulla porta " + port);
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
			String linea = null;
			byte[] data = null;
			/*
			 * 1) RICEZIONE DEL DATAGRAMMA
			 * 2) LETTURA E PARSING DEL CONTENUTO DEL DATAGRAMMA
			 * 3) PREPARAZIONE E INVIO RISPOSTA
			 */
			
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				//System.out.println("\nIn attesa di richieste...");
				
				
				/*
				 * 1) RICEZIONE DEL DATAGRAMMA
				 */
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

				/*
				 * 2) LETTURA E DEL CONTENUTO DEL DATAGRAMMA
				 */
				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					richiesta = diStream.readUTF();
					/*nomeFile = richiesta.split(" ")[0];
					numLinea = Integer.parseInt(richiesta.split(" ")[1]);*/
					System.out.println(richiesta);
				}
				catch (Exception e) {
					System.err.println("Problemi nella lettura della richiesta: "
						+ nomeFile + " " + numLinea);
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}

				/*
				 * 3) PREPARAZIONE E INVIO RISPOSTA
				 */
				try {
					System.out.print(nome + ": ");
					linea = nome + ": " + stdIn.readLine();
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(linea);
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

			} // while

		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il server termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("LineServer: termino...");
		socket.close();
	}
}