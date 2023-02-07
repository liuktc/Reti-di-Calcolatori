
/**
 * RegistryRemotoServer interfaccia del registry per il server.
 * */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistryRemotoServer extends RegistryRemotoClient {

  public boolean aggiungi(String nomeLogico, Remote riferimento) throws RemoteException;

  public Object[][] restituisciTutti() throws RemoteException;

  public boolean eliminaPrimo(String nomeLogico) throws RemoteException;

  public boolean eliminaTutti(String nomeLogico) throws RemoteException;

}
