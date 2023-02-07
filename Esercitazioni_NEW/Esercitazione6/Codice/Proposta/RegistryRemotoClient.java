
/**
 * RegistryRemotoClient --> interfaccia del registry per il client.
 * */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistryRemotoClient extends Remote {

	/**
	 * @param nomeLogico da cercare.
	 * @return il riferimento remoto cercato, oppure null.
	 * @throws RemoteException
	 */
	public Remote cerca(String nomeLogico) throws RemoteException;

	/**
	 * @param nomeLogico
	 * @return tutti i riferimenti corrispondenti ad un nome logico,
	 *         oppure un vettore vuoto.
	 * @throws RemoteException
	 */
	public Remote[] cercaTutti(String nomeLogico) throws RemoteException;

}
