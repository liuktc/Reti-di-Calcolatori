
/**associaT
 * RegistryRemotoTagServer --> interfaccia del registry per il server (metodi Tag).
 * */

import java.rmi.RemoteException;

public interface RegistryRemotoTagServer extends RegistryRemotoServer, RegistryRemotoTagClient {
	public boolean associaTags(String nomeLogico, String[] tags) throws RemoteException;
}
