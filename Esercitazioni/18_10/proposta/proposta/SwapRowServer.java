import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SwapRowServer extends Thread{
    
    int port = -1;
    String fileName = null;

    public SwapRowServer(int port, String fileName){
        this.port = port;
        this.fileName = fileName;
    }
    public SwapRowServer(){}

    public void run(){
        // Lo SwapRowServer viene inizializzato con una certa porta ed un certo file
        // deve creare una socket che possa ricevere le richieste dei clienti e fare
        // lo swap delle righe desiderate.
        DatagramSocket socket = null;
		DatagramPacket packet = null;
		byte[] buf = new byte[256];

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
			int riga1 = -1;
			int riga2 = -1;
			String riga1_value = null;
			String riga2_value = null;
			String richiesta = null;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			StringTokenizer st = null;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			String linea = null;
			byte[] data = null;
			String esito = "Successo";

            //andiamo in loop infinito, il server è un demone che esegue sempre in attesa di richieste
            while(true){
                System.out.println("\n[SwapRowServer] In attesa di richieste...");
				
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

                try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					richiesta = diStream.readUTF();
					st = new StringTokenizer(richiesta);
					riga1 = Integer.parseInt(st.nextToken());
					riga2 = Integer.parseInt(st.nextToken());
					System.out.println("[SwapRowServer] Richiesta di fare swap delle righe (" + riga1 + ";" + riga2 + ")");
				}
				catch (Exception e) {
					System.err.println("Problemi nella lettura della richiesta");
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}

				// preparazione della linea e invio della risposta
				try {
					/*
					 * ALGORITMO DI SCAMBIO DELLE RIGHE DEL FILE
					 */
					File inputFile = new File(fileName);
					BufferedReader in = new BufferedReader(new FileReader(inputFile));

					linea = in.readLine();
					int numRiga = 1;
					// Faccio una prima lettura per estrarre le due righe di interesse
					// basta che numRiga sia minore di uno dei due per continuare a leggere
					while(linea != null && (numRiga <= riga2 || numRiga <= riga1)){
						if(numRiga == riga1){
							riga1_value = linea;
						}else if(numRiga == riga2){
							riga2_value = linea;
						}
						linea = in.readLine();
						numRiga++;
					}
					if(riga1 < 0 || riga2 < 0 || riga1_value == null || riga2_value == null){
						esito = "Fallimento, numero di righe sbagliato";
					}

					// Ora rileggo tutto il fileInput e lo scrivo in un file temp e 
					// quando raggiungo la riga da scambiare scrivo l'altra salvata
					in = new BufferedReader(new FileReader(inputFile));
					// Creo il file temp, dove mettero il mio output temporaneo
					File fileTemp = new File("temp");
					FileWriter fout = new FileWriter(fileTemp);

					linea = in.readLine();
					numRiga = 1;
					while(linea != null){
						if(numRiga == riga1){
							fout.write(riga2_value + System.lineSeparator());
						}else if(numRiga == riga2){
							fout.write(riga1_value + System.lineSeparator());
						}else{
							fout.write(linea + System.lineSeparator());
						}
						linea = in.readLine();
						numRiga++;
					}

					in.close();
					fout.close();
					
					/*
					 * CODICE DA CONTROLLARE: In teoria sovrascrive il vecchio input con il temp output
					 */
					inputFile.delete();
					File dest = new File(fileName);
					fileTemp.renameTo(dest);
					/*
					 * -------------------------------------------------------------------------------
					 */
				
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(esito);
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
        System.out.println("[SwapRowServer] Termino...");
		socket.close();
    }

    public void SetFile(String fileName){
        this.fileName = fileName;
    }

    public void SetPort(int port){
        this.port = port;
    }
}
