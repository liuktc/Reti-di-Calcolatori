/* Nome Cognome Matricola */

import java.io.*;
import java.net.*;

public class Client {

	public static void main(String[] args) {

		InetAddress addr = null;
		int port = -1;
			
        /* Controllo dei parametri di invocazione */
		try {
			if (args.length == 2) {
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

		/*
         * Creazione della socket datagram, settaggio timeout di 30s
         * e creazione datagram packet.
         */
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
		System.out
			.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");

		try {
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			byte[] data = null;
			String nomeFile = null;
			int numLinea = -1;
			String richiesta = null;
			String risposta = null;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
            
            /* Ciclo infinito di richieste all'utente */
			while ((nomeFile = stdIn.readLine()) != null) {
				try {
					System.out.print("Numero della linea? ");
					numLinea = Integer.parseInt(stdIn.readLine());
					richiesta = nomeFile + " " + numLinea;
				} catch (Exception e) {
					System.out.println("Problemi nell'interazione da console: ");
					e.printStackTrace();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
					continue;
				}

				try {
                    /* Conversione della stringa richiesta in array di byte */
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(richiesta);
					data = boStream.toByteArray();
					
                    /* Invio della richiesta al server */
                    packet.setData(data);
					socket.send(packet);
					System.out.println("Richiesta inviata a " + addr + ", " + port);
				} catch (IOException e) {
					System.out.println("Problemi nell'invio della richiesta: ");
					e.printStackTrace();
					System.out
				      .print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
					continue;
				}

				try {
					/* Settaggio buffer e ricezione del messaggio del server */
                    /* Se scade il timeout impostato (30s) allora viene lanciata */
                    /* l'eccezzione SocketTimeoutException */
					packet.setData(buf);
					socket.receive(packet);

					/* SocketTimeoutException deriva da IOException, quindi così viene catturata */
				} catch (IOException e) {
					System.out.println("Problemi nella ricezione del datagramma: ");
					e.printStackTrace();
					System.out
						.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
					continue;
				}

				try {
                    /* Conversione della risposta da array di byte a stringa */
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					risposta = diStream.readUTF();
					System.out.println("Risposta: " + risposta);
				} catch (IOException e) {
					System.out.println("Problemi nella lettura della risposta: ");
					e.printStackTrace();
					System.out
				      .print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
					continue;
				}
			
				System.out
			    	.print("\n^D(Unix)/^Z(Win)+invio per uscire, altrimenti inserisci nome file (con estensione): ");
			}
		}
		/*
         * Qui catturo le eccezioni non catturate all'interno del while
         * in seguito alle quali il client termina l'esecuzione.
         */ 
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("LineClient: termino...");
		socket.close();
	}
}