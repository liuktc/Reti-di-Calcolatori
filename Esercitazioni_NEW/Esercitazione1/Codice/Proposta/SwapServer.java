// SwapServer.java

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class SwapServer extends Thread {

	private DatagramSocket socket = null;
	private DatagramPacket packet = null;
	private byte[] buf = new byte[256];
	private int port = -1;
	private String nomeFile = null;

	public SwapServer(File f, int p) {
		nomeFile = f.getName();
		port = p;
	}

	public void run() {
		try {
			//apertura Socket
			socket = new DatagramSocket(port);
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("SwapServer per file " + nomeFile + " avviato con socket port: " + socket.getLocalPort()); 
		} catch (SocketException e) {
			System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			byte req = (byte) 0;
			int result = 0;
			byte[] data = null;
			String mask = null;
			int firstRow=0, secondRow=0;

			while (true) {
				System.out.println("\nSwapServer in attesa di richieste...");

				// ricezione del datagramma
				try {
					packet.setData(buf);
					socket.receive(packet);

				} catch (IOException e) {
					System.err.println("Problemi nella ricezione del datagramma: "+ e.getMessage());
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}

				// Estrazione prima e seconda riga da scambiare
				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					String firstAndSecondRows = diStream.readUTF();
					StringTokenizer st = new StringTokenizer(firstAndSecondRows, "-");
					firstRow = Integer.parseInt((String)st.nextElement());
					secondRow = Integer.parseInt((String)st.nextElement());			
					System.out.println("FirstRow: " + firstRow + " SecondRow: " + secondRow);
				}
				catch (IOException e) {
					System.out.println("Problemi nella lettura della risposta: ");
					e.printStackTrace();
				}

				// swape invio dell'esito
				try {
					// Swap parole
					BufferedReader br = null;
					BufferedWriter bw = null;
					String line;
					int count = 0;
					String firstLineCache = null;
					String secondLineCache = null;

					System.out.println("Swapping rows...");
					// associazione di uno stream di input al file da cui estrarre la parola
					br = new BufferedReader(new FileReader(nomeFile));
					bw = new BufferedWriter(new FileWriter("temp"));
					
					// Read the file once to get the lines
					while ((line = br.readLine())!=null) {
						count++;
						if (count == firstRow) {
							firstLineCache = line;
						} else if(count == secondRow){
							secondLineCache = line;
						}
					}
					br.close();
					//TODO: controllare se le linee esistono effettivamente
					if(firstLineCache == null || secondLineCache == null){
						result = -1;
					}
				
					// Read the file a second time to swap the lines
					br = new BufferedReader(new FileReader(nomeFile));
					count = 0;
					while ((line = br.readLine())!=null){
						count++;
						if (count == firstRow) {
							line = secondLineCache;
						} else if(count == secondRow){
							line = firstLineCache;
						}
						bw.write(line + "\n");
					}
					bw.close(); 
					br.close();
				
					// Rename the file
					// 1- Delete the old file
					File fileorig = new File(nomeFile);
					fileorig.delete();
					// 2 - Create a new file with the same name
					File file = new File(nomeFile);
					File tempFile = new File("temp");		
					// 3 - Call rename and delete the temp file
					tempFile.renameTo(file);
					tempFile.delete();		
					// TODO: controllare l'esito di queste operazioni
					result = 0;	
					// Preparo l'esito
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeInt(result);
					data = boStream.toByteArray();
					packet.setData(data, 0, data.length);
					socket.send(packet);
				} catch (IOException e) {
					System.err.println("Problemi, i seguenti: "+ e.getMessage());
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
		System.out.println("SwapServer: termino...");
		socket.close();
	}
}