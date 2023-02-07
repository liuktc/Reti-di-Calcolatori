
/**
 * ServerImple.java
 * 	Implementa i servizi dichiarati in RemOp.
 * */

import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerImpl extends UnicastRemoteObject implements RemOp {

	// Costruttore
	public ServerImpl() throws RemoteException {
		super();
	}

	public int conta_righe(String fileName, int wordsNum) throws RemoteException {
		FileReader in = null;
		int wordsCount = 0, x, ret = 0;

		// "prev" serve per identificare se il precendente carattere era un
		// separatore: in tal caso, non dobbiamo contare una nuova parola!
		char ch, prev = ' ';

		try {
			in = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			throw new RemoteException(e.toString());
		}

		try {
			while ((x = in.read()) >= 0) {
				ch = (char) x;
				// TODO Controllare il caso di multiple interruzioni di riga
				if (ch == '\n') {// new line
					if (wordsCount > wordsNum) {
						ret++;
					}
					wordsCount = 0;
				}
				// Qui ci limitiamo ai separatori "di base".
				// Come potremmo fare ad aggiungere un numero arbitrario
				// (magari riconfigurabile) di separatori?
				if (ch == ' ' || ch == '\r') {
					if (prev == ' ' || prev == '\n' || prev == '\r')
						;
					else {// nuova parola
						wordsCount++;
					}
				}
				prev = ch;
			} // while
		} catch (IOException e) {
			throw new RemoteException(e.toString());
		}
		try {
			in.close();
		} catch (IOException e) {
			throw new RemoteException(e.toString());
		}
		return ret;
	}

	public int elimina_riga(String fileName, int rowNum) throws RemoteException {
		String line;
		String outFileName = fileName.substring(0, (fileName.length() - 4)) + "_modified.txt";
		BufferedReader fileIn;
		File out, in;
		FileWriter fileOut;
		int rowsCount = 0;

		try {
			fileIn = new BufferedReader(new FileReader(fileName));
			out = new File(outFileName);
			fileOut = new FileWriter(out);
		} catch (FileNotFoundException e) {
			throw new RemoteException(e.toString());
		} catch (IOException e) {
			throw new RemoteException(e.toString());
		}

		try {
			rowsCount = 0;
			while ((line = fileIn.readLine()) != null) {
				rowsCount++;
				if (rowsCount != rowNum) {
					fileOut.write(line + "\n");
				}
			}
			fileIn.close();
			fileOut.close();

			// Replace the original file with the tmp
			in = new File(fileName);
			in.delete();
			out.renameTo(in);

		} catch (IOException e) {
			throw new RemoteException(e.toString());
		}
		if (rowsCount < rowNum) {
			throw new RemoteException(
					"Il file remoto scelto ha " + rowsCount + " righe. Inserire un numero minore di " + rowNum);
		} else {
			return rowsCount - 1;
		}
	}

	public static void main(String[] args) {

		int registryPort = 1099;
		String registryHost = "localhost";
		String serviceName = "Server";

		// Controllo parametri
		if (args.length != 0 && args.length != 1) {
			System.out.println("Sintassi: ServerImpl [registryPort]");
			System.exit(1);
		}
		if (args.length == 1) {
			try {
				registryPort = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Sintassi: ServerImpl [registryPort], registryPort intero");
				System.exit(2);
			}
		}

		// Registrazione del servizio RMI
		String completeName = "//" + registryHost + ":" + registryPort + "/" + serviceName;
		try {
			ServerImpl serverRMI = new ServerImpl();
			Naming.rebind(completeName, serverRMI);
			System.out.println("Server RMI: Servizio \"" + serviceName + "\" registrato");
		} catch (Exception e) {
			System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}