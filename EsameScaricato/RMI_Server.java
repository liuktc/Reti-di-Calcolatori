//S0000971128, Dominici, Leonardo
package RMI;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_InterfaceFile{

	public static Bikes bikeStorage;
	
	protected RMI_Server() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int elimina_prenotazione(int giorno, int mese, int anno) throws RemoteException {
		int finalResult;
		try {
			finalResult = bikeStorage.elimina_prenot(giorno, mese, anno);
		}catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
		return finalResult;
	}

	@Override
	public Object[][] visualizza_prenotazioni(int giorno, int mese, int anno) throws RemoteException {
		Object[][] finalResult;
		try {
			finalResult = bikeStorage.visual_prenot(giorno, mese, anno);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RemoteException();
		}
		if(finalResult == null) {
			throw new RemoteException();
		}else {
			return finalResult;
		}
	}

	public static void main(String args[]) {
		bikeStorage = new Bikes();	
		
		final int REGISTRY_PORT = 1099;
		String registryHost = "localhost";
		String serviceName = "RMI_Server";
		
		try{
			// Registrazione del servizio RMI
			String completeName = "//" + registryHost + ":" + REGISTRY_PORT + "/" + serviceName;
			RMI_Server serverRMI = new RMI_Server();
			Naming.rebind (completeName, serverRMI);
			System.out.println("Tutto pronto per partire, sono carico!");
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
}
