import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemOp extends Remote {
    boolean aggiungi_stanza(String nomeStanza, char tipoComunicazione) throws RemoteException;
    Stanza[] elimina_utente(String nomeUtente) throws RemoteException;
}