// MulticastClient.java

import java.io.*;
import java.net.*;

public class MulticastClient {

	public static void main(String[] args) {
	    InetAddress group = null;
	    int port = -1;
	    try{
	    	if(args.length == 2){
	    		group = InetAddress.getByName(args[0]);
	    		port = Integer.parseInt(args[1]);
	    		System.out.println("Mi pongo in ascolto su:\nMCastAddr: " + args[0]
	    			+ "\nMCastPort: " + port);
	    	}
	    	else{
	    		System.out.println("Usage: java MulticastClient MCastAddr MCastPort");
	    		System.exit(1);
	    	}
	    } //try
	    catch(Exception e){
	    	System.out.println("Problemi, i seguenti: ");
	    	e.printStackTrace();
	    	System.exit(2);
	    }

	    // creazione della socket multicast e del datagram packet
	    MulticastSocket socket = null;
	    byte[] buf = new byte[256];
	    DatagramPacket packet = null;
	
	    try{
	    	socket = new MulticastSocket(port);
	    	socket.setSoTimeout(20000); // 20 secondi
	    	packet = new DatagramPacket(buf, buf.length);
	    	System.out.println("\nMulticastClient: avviato");
	    	System.out.println("Creata la socket: " + socket);
	    }
	    catch(IOException e){
	    	System.out.println("Problemi nella creazione della socket: ");
	    	e.printStackTrace();
	    	System.exit(3);
	    }

	    try{
	    	/* Per evitare problemi, con computer non connessi in rete,
	    	 * bisogna impostare l'interfaccia di rete PRIMA di fare la
	    	 * join al gruppo.
	    	 *
	    	 * VEDERE ANCHE LE FAQ nel sito!!!
	    	 */
	   
	    	socket.joinGroup(group); // adesione al gruppo associato all'indirizzo multicast
	    	System.out.println("Adesione al gruppo " + group);
	    }
	    catch(IOException e){
	    	System.out.println("Problemi nell'adesione al gruppo: ");
	    	e.printStackTrace();
	    	System.exit(4);
	    }

	    ByteArrayInputStream biStream = null;
	    DataInputStream diStream = null;
	    String linea = null;
	
	    // ciclo ricezione
	    for(int i = 0; i < 20; i++){
	
	    	System.out.println("\nIn attesa di un datagramma... ");
	    	try{
	    		packet.setData(buf);
	    		socket.receive(packet);
	    	}
	    	catch(SocketTimeoutException te){
	    		System.out.println("Non ho ricevuto niente per 20 secondi, chiudo!");
	    		System.exit(5);
	    	}
	    	catch(IOException e){
	    		System.out.println("Problemi nella ricezione del datagramma: ");
	    		e.printStackTrace();
	    		// anche se ci sono problemi riprende il ciclo di ricezioni
	    		continue;
	    	}
	    	try{
	    		biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
	    		diStream = new DataInputStream(biStream);
	    		linea = diStream.readUTF();
	    		System.out.println("Linea ricevuta: " + linea);
	    	}
	    	catch(IOException e){
	    		System.out.println("Problemi nella lettura del datagramma: ");
	    		e.printStackTrace();
	    		continue;
	    		// anche se ci sono problemi riprende il ciclo di ricezioni
	      }
	    } //for

	    // uscita dal gruppo e chiusura della socket
	    System.out.println("\nUscita dal gruppo");
	    try{
	    	socket.leaveGroup(group);
	    }
	    catch(IOException e){
	    	System.out.println("Problemi nell'uscita dal gruppo: ");
	    	e.printStackTrace();
	    }
	    
	    System.out.println("MulticastClient: termino...");
	    socket.close();
	}
}