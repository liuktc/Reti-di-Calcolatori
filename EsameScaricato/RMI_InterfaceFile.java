//S0000971128, Dominici, Leonardo
package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_InterfaceFile extends Remote{

	public int elimina_prenotazione(int giorno, int mese, int anno) throws RemoteException;
	
	public Object[][] visualizza_prenotazioni(int giorno, int mese, int anno) throws RemoteException;
	
}
