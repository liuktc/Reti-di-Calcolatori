
/**
 * ServerCongressoImpl.java
 * 
 * */

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerCongressoImpl extends UnicastRemoteObject implements
		ServerCongresso {
	static Programma prog[];

	// Costruttore
	public ServerCongressoImpl() throws RemoteException {
		super();
	}

	// Richiede una prenotazione
	public int registrazione(int giorno, String sessione, String speaker)
			throws RemoteException {
		int numSess = -1;
		System.out.println("Server RMI: richiesta registrazione con parametri");
		System.out.println("giorno   = " + giorno);
		System.out.println("sessione = " + sessione);
		System.out.println("speaker  = " + speaker);

		if (sessione.startsWith("S")) {
			try {
				numSess = Integer.parseInt(sessione.substring(1)) - 1;
			} catch (NumberFormatException e) {
			}
		}

		// Se i dati sono sbagliati significa che sono stati trasmessi male e quindi
		// solleva una eccezione
		if (numSess == -1)
			throw new RemoteException();
		if (giorno < 1 || giorno > 3)
			throw new RemoteException();

		return prog[giorno - 1].registra(numSess, speaker);
	}

	// Ritorno il campo
	public Programma programma(int giorno) throws RemoteException {
		System.out.println("Server RMI: richiesto programma del giorno " + giorno);
		return prog[giorno - 1];
	}

	// Avvio del Server RMI
	public static void main(String[] args) {

		// creazione programma
		prog = new Programma[3];
		for (int i = 0; i < 3; i++)
			prog[i] = new Programma();
		int registryRemotoPort = 1099;
		String registryRemotoName = "RegistryRemoto";
		String serviceName = "ServerCongresso";
		String[] tags = { "congresso", "registrazione", "programma", "speaker" };

		// Controllo dei parametri della riga di comando
		if (args.length != 1 && args.length != 2) {
			System.out
					.println(
							"Sintassi: ServerCongressoImpl NomeHostRegistryRemoto [registryPort], registryPort intero");
			System.exit(1);
		}
		String registryRemotoHost = args[0];
		if (args.length == 2) {
			try {
				registryRemotoPort = Integer.parseInt(args[1]);
			} catch (Exception e) {
				System.out
						.println(
								"Sintassi: ServerCongressoImpl NomeHostRegistryRemoto [registryPort], registryPort intero");
				System.exit(2);
			}
		}

		// Impostazione del SecurityManager
		// if (System.getSecurityManager() == null)
		// System.setSecurityManager(new RMISecurityManager());

		// Registrazione del servizio RMI
		String completeRemoteRegistryName = "//" + registryRemotoHost + ":"
				+ registryRemotoPort + "/" + registryRemotoName;

		// 1 --> lookup del registry remoto
		try {
			RegistryRemotoTagServer registryRemoto = (RegistryRemotoTagServer) Naming
					.lookup(completeRemoteRegistryName);
			// 2 --> aggiunta del nome logico al riferimento
			ServerCongressoImpl serverRMI = new ServerCongressoImpl();
			registryRemoto.aggiungi(serviceName, serverRMI);
			// 3 --> associazione TAG al nome logico
			registryRemoto.associaTags(serviceName, tags);
			System.out.println("Server RMI: Servizio \"" + serviceName
					+ "\" registrato");
		} catch (Exception e) {
			System.err.println("Server RMI \"" + serviceName + "\": "
					+ e.getMessage());
			if (e.getMessage().equals(""))
				System.err.println("Uno dei Tag di iscrizione è stato rifiutato!!!");
			e.printStackTrace();
			System.exit(1);
		}
	}// main
}