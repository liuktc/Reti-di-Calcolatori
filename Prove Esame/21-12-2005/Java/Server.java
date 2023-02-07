import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Server extends UnicastRemoteObject implements RemOp{
    static Stanza[] stanze;
    final static int NUM_STANZE = 5;

    public Server() throws RemoteException{
        super();
    }    

    boolean aggiungi_stanza(String nomeStanza, char tipoComunicazione) throws RemoteException{
        boolean trovato = false;
        if(tipoComunicazione != 'M' && tipoComunicazione != 'P')
            return false;
        // Controllo stanza con stesso nome gia presente
        for(int i=0;i<NUM_STANZE;i++){
            if(stanze[i].getNome().equals(nomeStanza)){
                return false;
            }
        }

        for(int i=0;i<NUM_STANZE;i++){
            if(stanze[i].getNome().equals("L")){
                stanze[i].setNome(nomeStanza);
                stanze[i].setStato(tipoComunicazione);
                trovato = true;
                return trovato;
            }
        }
        return trovato;
    }

    Stanza[] elimina_utente(String nomeUtente) throws RemoteException{
        int numeroStanze = 0;
        for(int i=0;i<NUM_STANZE;i++){
            if(stanze[i].containsUser(nomeUtente)){
                numeroStanze++;
            }
        }
        if(numeroStanze == 0){
            return null;
        }

        Stanza[] res = new Stanza[numeroStanze];
        numeroStanze = 0;
        for(int i=0;i<NUM_STANZE;i++){
            if(stanze[i].containsUser(nomeUtente)){
                stanze[i].removeUser(nomeUtente);
                res[numeroStanze] = stanze[i];
                numeroStanze++;
            }
        }
        return res;
    }

    public static void main(String[] args){
        int port = 1099;
        String host = "localhost";
        String serviceName = "RemOp";

        // Controllo dei parametri della riga di comando
		if (args.length != 0 && args.length != 1) {
			System.out.println("Sintassi: Server [port]");
			System.exit(1);
		}
		if (args.length == 1) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out
						.println("Sintassi: Server [port], port intero");
				System.exit(2);
			}
		}

        stanze = new Stanza[NUM_STANZE];
        for(int i=0;i<NUM_STANZE;i++){
            stanze[i] = new Stanza();
        }

        // Impostazione del SecurityManager
		if (System.getSecurityManager() == null)
            System.setSecurityManager(new RMISecurityManager());

        // Registrazione del servizio RMI
		String completeName = "//" + host + ":" + port + "/" + serviceName;

        try {
			Server serverRMI = new Server();
			Naming.rebind(completeName, serverRMI);
			System.out.println("Server RMI: Servizio \"" + serviceName + "\" registrato");
		} catch (Exception e) {
			System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
    }
}
