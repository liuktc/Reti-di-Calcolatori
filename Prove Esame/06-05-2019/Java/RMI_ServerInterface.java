import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_ServerInterface extends Remote{
    String[] lista_file(String nome_dir) throws RemoteException;

    int numerazione_righe(String nome_file) throws RemoteException;
}
