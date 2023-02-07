//SwapClient.java

import java.io.*;
import java.net.*;

public class SwapClient {
	public static void main(String[] args) {
		InetAddress addr = null;
		int discoveryPort = -1;
		String fileName = null;

		// Check args
		try {
			if (args.length == 3) {
				addr = InetAddress.getByName(args[0]);
				discoveryPort = Integer.parseInt(args[1]);
				fileName = args[2];
				System.out.println("Interrogo il discovery server:\nIndirizzo: " + args[0] + "\nporta: " 
					+ discoveryPort + "\nfile: " + fileName);
			} else {
				System.out.println("Usage: java SwapClient ipDiscoveryServer portDiscoveryServer fileName");
				System.exit(1);
			}
		}
		catch (Exception e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
			System.exit(1);
		}

		//creazione e inizializzazione socket
		DatagramSocket datagramSocket = null;
		DatagramPacket datagramPacket = null;
		byte[] buf = new byte[256];
		try {
			datagramSocket = new DatagramSocket();
			datagramSocket.setSoTimeout(30000);
			datagramPacket = new DatagramPacket(buf, buf.length, addr, discoveryPort);
			System.out.println("\nSwapClient: avviato");
			System.out.println("Creata la socket: " + datagramSocket);
		}
		catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.out.println("SwapClient: interrompo...");
			System.exit(1);
		}

		int swapServerPort = -1;
		ByteArrayOutputStream boStream = null;
		DataOutputStream doStream = null;
		String risposta = null;
		ByteArrayInputStream biStream = null;
		DataInputStream diStream = null;
		byte[] data = null;

		try {
			// riempimento e invio del pacchetto (vuoto)
			try {
				boStream = new ByteArrayOutputStream();
				doStream = new DataOutputStream(boStream);
				doStream.writeUTF(fileName);
				data = boStream.toByteArray();
				datagramPacket.setData(data);
				datagramSocket.send(datagramPacket);
				System.out.println("Richiesta inviata a " + addr + ", " + discoveryPort);
			}
			catch (IOException e) {
				System.out.println("Problemi nell'invio della richiesta: ");
				e.printStackTrace();
			}

			try {// settaggio del buffer di ricezione
				datagramPacket.setData(buf);
				datagramSocket.receive(datagramPacket);
				// sospensiva solo per i millisecondi indicati, dopo solleva una SocketException
			}
			catch (IOException e) {
				System.out.println("Problemi nella ricezione del datagramma: ");
				e.printStackTrace();
			}

			try {//Estrazione risposta (porta o errore)
				biStream = new ByteArrayInputStream(datagramPacket.getData(), 0, datagramPacket.getLength());
				diStream = new DataInputStream(biStream);
				risposta = diStream.readUTF();
				System.out.println("Risposta(porta): " + risposta);
			}
			catch (IOException e) {
				System.out.println("Problemi nella lettura della risposta: ");
				e.printStackTrace();
			}
			if ( risposta.equals("File non trovato") ) {
				System.out.println("Errore, il seguente: ");
				System.out.println(risposta + "\nEsco...");
				datagramSocket.close();
				System.exit(4);
			} else {
				swapServerPort = Integer.parseInt(risposta);
			}
		}
		catch (Exception e) {
			System.out.println("Problemi nella ricezione dal Discovery Server: esco...");
			// si potrebbe gestire altrimenti l'eccezione, ad esempio tentando nuovamente
			e.printStackTrace();
			System.exit(5);
		}

		// riutilizzo stesse variabili
		datagramPacket = null;
		buf = new byte[256];
		datagramPacket = new DatagramPacket(buf, buf.length, addr, swapServerPort);
		boStream = new ByteArrayOutputStream();
		doStream = new DataOutputStream(boStream);
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Inserisci prima riga da scambiare contenuta nel file " + fileName +
			" oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
		try {
			String firstRow = null;
			String secondRow = null;
			int req;
			while ((firstRow = stdIn.readLine()) != null) {
				System.out.println("Inserisci seconda riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
				secondRow = stdIn.readLine();
				String firstAndSecondRows = firstRow + "-" + secondRow;

				//Send request
				try {					
					doStream.writeUTF(firstAndSecondRows);
					data = boStream.toByteArray();
					datagramPacket.setData(data);
					datagramSocket.send(datagramPacket);
					System.out.println("Richiesta inviata a " + addr + ", " + swapServerPort);
				} catch (IOException e) {
					System.out.println("Problemi nell'invio della richiesta: ");
					e.printStackTrace();
					System.out.println("Inserisci seconda riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
					continue;
				}

				//set buffer and receive answer
				try {
					datagramPacket.setData(buf);
					datagramSocket.receive(datagramPacket);
					// sospensiva solo per i millisecondi indicati, dopo solleva una SocketException
				} catch (IOException e) {
					System.out.println("Problemi nella ricezione del datagramma: ");
					e.printStackTrace();
					System.out.println("Inserisci seconda riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}
				try {
					biStream = new ByteArrayInputStream(datagramPacket.getData(), 0, datagramPacket.getLength());
					diStream = new DataInputStream(biStream);
					int esitoScambioRighe = diStream.readInt();
					System.out.println("Esito scambio righe: " + esitoScambioRighe);
					
				} catch (IOException e) {
					System.out.println("Problemi nella lettura della risposta: ");
					e.printStackTrace();
					System.out.println("Inserisci seconda riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
					continue;
					// il client continua l'esecuzione riprendendo dall'inizio del ciclo
				}
				// tutto ok, pronto per nuova richiesta
				System.out.println("Inserisci prima riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");
			} // while
		}
		// qui catturo le eccezioni non catturate all'interno del while
		// in seguito alle quali il client termina l'esecuzione
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\nSwapClient: termino...");
		datagramSocket.close();
	}
}