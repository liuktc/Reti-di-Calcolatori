/**
 * Interfaccia remota di servizio
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemOp extends Remote {

	/**
	 * @param nomeFile = nome del file remoto
	 * @param wordsNum = soglia di parole
	 * @throws RemoteException = FileNotFound, IOException
	 **/
	public int conta_righe (String fileName, int wordsNum) throws RemoteException;

	/**
	 * @param fileName = nome del file remoto
	 * @param rowNum = numero della riga da eliminare
	 * @throws RemoteException = = FileNotFound, IOException, numero riga troppo grande
	 * @return: int esito operazione
	 **/
	public int elimina_riga (String fileName, int rowNum) throws RemoteException;
}