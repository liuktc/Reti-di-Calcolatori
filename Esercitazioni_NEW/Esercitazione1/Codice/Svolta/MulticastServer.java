// MulticastServer.java

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastServer {

  /*
   * Il file da dove leggo si trova allo stesso livello, nel progetto Eclipse, 
   * 	delle cartelle src e bin. In questo modo posso referenziarlo senza usare l'intero path.
   */
	private static final String FILE = "saggezza.txt";
	private static final long WAIT = 1000;
	private static BufferedReader in = null;	
	private static boolean moreLines = true;
	
	public static void main(String[] args) {
			
	    InetAddress group = null;
	    int port = -1;
	    
	    try {
	    	if (args.length == 1) {
	    		group = InetAddress.getByName("230.0.0.1");
	    		try {
	    			port = Integer.parseInt(args[0]);
	    		} catch (NumberFormatException e) {
	    			System.out
	    				.println("Usage: \"java MulticastServer MCastPort\" or \"java MulticastServer MCastAddr MCastPort\"");
	    			System.exit(1);
	    		}
	    	} else if (args.length == 2) {
	    		group = InetAddress.getByName(args[0]);
	    		try {
	    			port = Integer.parseInt(args[1]);
	    		} catch (NumberFormatException e) {
	    			System.out
	    				.println("Usage: \"java MulticastServer MCastPort\" or \"java MulticastServer MCastAddr MCastPort\"");
	    			System.exit(1);
	    		}
	    	} else {
	    		System.out
	    			.println("Usage: \"java MulticastServer MCastPort\" or \"java MulticastServer MCastAddr MCastPort\"");
	    		System.exit(1);
	    	}
		} //try group and port
		catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.out
		      	.println("Usage: \"java MulticastServer MCastPort\" or \"java MulticastServer MCastAddr MCastPort\"");
			System.exit(1);
		}

		// Multicast socket
		MulticastSocket socket = null;
		byte[] data = new byte[256];
		DatagramPacket packet = new DatagramPacket(data, data.length, group, port);

	    // creazione della multicast socket e aggancio della multicast socket al
	    // gruppo
	    try {
	    	socket = new MulticastSocket(port);
	    	/* Per evitare problemi, con computer non connessi in rete,
	    	 * bisogna impostare l'interfaccia di rete PRIMA di fare la
	    	 * join al gruppo.
	    	 *
	    	 * VEDERE ANCHE LE FAQ nel sito!!!
	    	 */
	    	socket.joinGroup(group);
	    	System.out.println("Socket: " + socket);
	    } catch (Exception e) {
	    	System.out.println("Problemi nella creazione della socket: ");
	    	e.printStackTrace();
	    	System.exit(2);
	    }

	    System.out.println("MulticastServer: avviato\nMCastAddr: " + group
	        + "\nport: " + socket.getLocalPort());
	
	    int count = -1; //contatore debug
	    
	    ByteArrayOutputStream boStream = null;
	    DataOutputStream doStream = null;

	    while (true) {
	    	// azzero il contatore per il debug
	    	count = 0;
	    	moreLines = true;
	    	try {
	    		// costruisco un nuovo buffered reader
	    		in = new BufferedReader(new FileReader(FILE));
	    	} catch (Exception e) {
	    		System.out
	    			.println("MulticastServer, errore durante il reset del file reader, il seguente: "
	    					+ e + "\nEsco.");
	    		System.exit(3);
	    		// provo di nuovo
	    		continue;
	    	}

	    	// eseguo il ciclo su tutte le linee del file
	    	while (moreLines) {

	    		count++;

	    		// estrazione della linea
	    		String linea = LineUtility.getNextLine(in);
				if (linea.equals("Nessuna linea disponibile")) {
					moreLines = false;
					// esco dal ciclo di lettura/trasmissione per
					// iniziarne un altro
					break;
				}

				//DEBUG
				System.out.println("Estratta linea # " + count + " : " + linea);
				
				// costruzione del datagramma contenente la linea e invio della stessa
				try {
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(linea);
					data = boStream.toByteArray();
					packet.setData(data);
					socket.send(packet); // invio della linea al gruppo
				} catch (Exception e) {
					System.out.println("Problemi nell'invio del datagramma: ");
					e.printStackTrace();
					// continuo a leggere la linea seguente
					continue;
				}

				// attesa tra un invio e l'altro...
				try {
					Thread.sleep(2*WAIT);
				} catch (Exception e) {
					System.out
				      	.println("MulticastServer, errore durante la sleep, il seguente: "
				      			+ e);
					// continuo a leggere la linea seguente
					continue;
				} //catch
	    	} // while(moreLines)
	    } // while(true)
	} //main
}