
/**
 * Implementazione del RegistryRemoto
 */

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RegistryRemotoImpl extends UnicastRemoteObject implements
		RegistryRemotoTagServer {

	final int tableSize = 100;
	private static final String[] VALID_TAGS = {
			"registrazione",
			"congresso",
			"TAG3",
			"ristorante",
			"YetAnotherTag"
	};

	Object[][] table = new Object[tableSize][3];

	public RegistryRemotoImpl() throws RemoteException {
		super();
		for (int i = 0; i < tableSize; i++) {
			table[i][0] = null;
			table[i][1] = null;
			table[i][2] = null;
		}
	}

	/**
	 * @return --> Aggiunge la coppia nella prima posizione disponibile
	 */
	public synchronized boolean aggiungi(String nomeLogico, Remote riferimento) throws RemoteException {
		// Cerco la prima posizione libera e la riempio
		boolean risultato = false;
		if ((nomeLogico == null) || (riferimento == null)) {
			return risultato;
		}
		for (int i = 0; i < tableSize; i++) {
			if (table[i][0] == null) {
				table[i][0] = nomeLogico;
				table[i][1] = riferimento;
				risultato = true;
				break;
			}
		}
		return risultato;
	}

	/**
	 * @return --> riferimento remoto cercato, oppure false
	 */
	public synchronized Remote cerca(String nomeLogico) throws RemoteException {
		Remote risultato = null;
		if (nomeLogico == null) {
			return null;
		}
		for (int i = 0; i < tableSize; i++) {
			if (nomeLogico.equals((String) table[i][0])) {
				risultato = (Remote) table[i][1];
				break;
			}
		}
		return risultato;
	}

	/**
	 * @return --> riferimenti corrispondenti ad un nome logico, oppure false
	 */
	public synchronized Remote[] cercaTutti(String nomeLogico) throws RemoteException {
		int cont = 0;
		if (nomeLogico == null) {
			return new Remote[0];
		}
		for (int i = 0; i < tableSize; i++) {
			if (nomeLogico.equals((String) table[i][0])) {
				cont++;
			}
		}
		Remote[] risultato = new Remote[cont];
		// Ora lo uso come indice per il riempimento
		cont = 0;
		for (int i = 0; i < tableSize; i++) {
			if (nomeLogico.equals((String) table[i][0])) {
				risultato[cont++] = (Remote) table[i][1];
			}
		}
		return risultato;
	}

	/**
	 * @return --> tutti i riferimenti, oppure false
	 */
	public synchronized Object[][] restituisciTutti() throws RemoteException {
		int cont = 0;
		for (int i = 0; i < tableSize; i++) {
			if (table[i][0] != null) {
				cont++;
			}
		}
		Object[][] risultato = new Object[cont][2];
		// Ora lo uso come indice per il riempimento
		cont = 0;
		for (int i = 0; i < tableSize; i++) {
			if (table[i][0] != null) {
				risultato[cont][0] = table[i][0];
				risultato[cont][1] = table[i][1];
			}
		}
		return risultato;
	}

	public synchronized boolean eliminaPrimo(String nomeLogico)
			throws RemoteException {
		boolean risultato = false;
		if (nomeLogico == null)
			return risultato;
		for (int i = 0; i < tableSize; i++)
			if (nomeLogico.equals((String) table[i][0])) {
				table[i][0] = null;
				table[i][1] = null;
				risultato = true;
				break;
			}
		return risultato;
	}

	public synchronized boolean eliminaTutti(String nomeLogico)
			throws RemoteException {
		boolean risultato = false;
		if (nomeLogico == null)
			return risultato;
		for (int i = 0; i < tableSize; i++)
			if (nomeLogico.equals((String) table[i][0])) {
				if (risultato == false)
					risultato = true;
				table[i][0] = null;
				table[i][1] = null;
			}
		return risultato;
	}

	// cerca i servizi che fanno match con tutti i tag della lista
	public synchronized String[] cercaTag(String[] tag) throws RemoteException {
		int dimlist = 0;
		String[] test = null;
		for (int i = 0; i < tableSize && table[i][0] != null; i++) {
			int trovato = 0;
			String[] tmp = (String[]) table[i][2];
			for (int j = 0; j < tmp.length; j++) {
				for (int z = 0; z < tag.length; z++) {
					if (tmp[j].equals(tag[z])) {
						trovato++;
						break;
					}
				}
				if (trovato == tag.length) {
					dimlist++;
					break;
				}
			}
		}
		test = new String[dimlist];
		dimlist = 0;
		for (int i = 0; i < tableSize && table[i][0] != null; i++) {
			int trovato = 0;
			String[] tmp = (String[]) table[i][2];
			for (int j = 0; j < tmp.length; j++) {
				for (int z = 0; z < tag.length; z++) {
					if (tmp[j].equals(tag[z])) {
						trovato++;
						break;
					}
				}
				if (trovato == tag.length) {
					test[dimlist++] = (String) table[i][0];
					break;
				}
			}
		}
		return test;
	}

	// associa dei tag ad un nome logico di servizio
	public synchronized boolean associaTags(String nomeLogico, String[] tags) throws RemoteException {
		boolean risultato = false;
		for (String tempServerTag : tags) {
			for (String tempRegTag : VALID_TAGS) {
				if (tempServerTag.equals(tempRegTag)) {
					risultato = true;
					continue;
				}
			}
			if (risultato == false)
				throw new RemoteException("\"" + tempServerTag + "\"" + " è un TAG non valido");
		}

		risultato = false;
		if ((nomeLogico == null) || (tags == null))
			return risultato;
		for (int i = 0; i < tableSize; i++)
			if (nomeLogico.equals((String) table[i][0])) {
				table[i][2] = tags;
				risultato = true;
			}
		return risultato;
	}
		// Avvio del Server RMI
	public static void main(String[] args) {

		int registryRemotoPort = 1099;
		String registryRemotoHost = "localhost";
		String registryRemotoName = "RegistryRemoto";

		// Controllo dei parametri della riga di comando
		if (args.length != 0 && args.length != 1) {
			System.out.println("Sintassi: ServerImpl [registryPort]");
			System.exit(1);
		}
		if (args.length == 1) {
			try {
				registryRemotoPort = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out
						.println("Sintassi: ServerImpl [registryPort], registryPort intero");
				System.exit(2);
			}
		}

		// Impostazione del SecurityManager
		if (System.getSecurityManager() == null)
		System.setSecurityManager(new RMISecurityManager());

		// Registrazione del servizio RMI
		String completeName = "//" + registryRemotoHost + ":" + registryRemotoPort
				+ "/" + registryRemotoName;
		try {
			RegistryRemotoImpl serverRMI = new RegistryRemotoImpl();
			Naming.rebind(completeName, serverRMI);
			System.out.println("Server RMI: Servizio \"" + registryRemotoName
					+ "\" registrato");
		} catch (Exception e) {
			System.err.println("Server RMI \"" + registryRemotoName + "\": "
					+ e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
