import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_InterfaceFile extends Remote {

	int elimina_occorrenze(String nomeFile) throws RemoteException;

	String[] lista_filetesto(String dir) throws RemoteException;

}