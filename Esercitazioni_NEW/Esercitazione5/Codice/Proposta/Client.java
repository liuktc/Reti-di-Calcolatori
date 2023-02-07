/**
 * Client.java
 * */

import java.rmi.*;
import java.io.*;

class Client {

	public static void main(String[] args) {
		int registryPort = 1099;
		String registryHost = null;
		String serviceName = "Server";
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		// Controllo parametri
		if (args.length != 1 && args.length != 2) {
			System.out.println("Sintassi: ClientFile RegistryHost [registryPort]");
			System.exit(1);
		} else {
			registryHost = args[0];
			if (args.length == 2) {
				try {
					registryPort = Integer.parseInt(args[1]);
				} catch (Exception e) {
					System.out
					.println("Sintassi: ClientFile NomeHost [registryPort], registryPort intero");
					System.exit(1);
				}
			}
		}

		// Connessione al servizio RMI remoto
		try {
			String completeName = "//" + registryHost + ":" + registryPort + "/"
					+ serviceName;
			RemOp serverRMI = (RemOp) Naming.lookup(completeName);
			System.out.println("Client RMI: Servizio \"" + serviceName
					+ "\" connesso");

			System.out.println("\nRichieste di servizio fino a fine file");

			String service;
			System.out.print("Servizio (C=conta, E=Elimina): ");

			while ((service = stdIn.readLine()) != null) {

				if (service.equals("C")) {

					String nomeFile;
					System.out.print("Nome file? ");
					nomeFile = stdIn.readLine();

					//TODO Check int
					int wordsNum;
					System.out.print("Soglia parole: ");
					wordsNum=Integer.parseInt(stdIn.readLine());

					// Invocazione remota
					try {
						int rawsNum = serverRMI.conta_righe(nomeFile, wordsNum);
						System.out.println("Il file " + nomeFile + " contiene "
								+ rawsNum + " righe formate da un numero di parole > di "
										+ wordsNum);
					} catch (RemoteException re) {
						System.out.println("Errore remoto: " + re.toString());
					}

				} // C

				else
					if (service.equals("E")) {

						String nomeFile;
						System.out.print("Nome file? ");
						nomeFile = stdIn.readLine();

						//TODO Check int
						int lineNum;
						System.out.print("Numero linea? ");
						lineNum=Integer.parseInt(stdIn.readLine());

						int res = 0;
						try {
							res = serverRMI.elimina_riga(nomeFile, lineNum);
							System.out.println("L'eliminazione della riga "+lineNum+
								"ha dato come esito "+res+"!\n");
						} catch (RemoteException re) {
							System.out.println("Errore remoto: " + re.toString());
						}
					} // S

					else System.out.println("Servizio non disponibile");

				System.out.print("Servizio (C=conta, E=Elimina): ");
			} // !EOF

		} catch (Exception e) {
			System.err.println("ClientRMI: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}