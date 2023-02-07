import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;

public class Client {

    public static void main(String[] args){
        int port = 1099;
		String host = null;
		String serviceName = "RemOp";
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        // Controllo parametri
		if (args.length != 1 && args.length != 2) {
			System.out.println("Sintassi: Client host [port]");
			System.exit(1);
		} else {
			host = args[0];
			if (args.length == 2) {
				try {
					port = Integer.parseInt(args[1]);
				} catch (Exception e) {
					System.out
					.println("Sintassi: Client host [port], port intero");
					System.exit(1);
				}
			}
		}

        // Connessione al servizio RMI remoto
		try {
			String completeName = "//" + host + ":" + port + "/" + serviceName;
			RemOp serverRMI = (RemOp) Naming.lookup(completeName);
			System.out.println("Client RMI: Servizio \"" + serviceName + "\" connesso");

			System.out.println("\nRichieste di servizio fino a fine file");

            String service;
			System.out.print("Servizio (A=aggiungi stanza, E=Elimina utente): ");

			while ((service = stdIn.readLine()) != null) {
                if(service.equals("A")){
                    String nomeStanza;
					System.out.print("Nome stanza:");
					nomeStanza = stdIn.readLine();

                    String tipoComunicazione;
					System.out.print("Tipo comunicazione(P/M):");
					tipoComunicazione = stdIn.readLine();
                    while(!tipoComunicazione.equals("P") && !tipoComunicazione.equals("M")){
                        System.out.println("Inserire P o M!!!");
                        System.out.print("Tipo comunicazione(P/M):");
					    tipoComunicazione = stdIn.readLine();
                    }

                    boolean risultato = serverRMI.aggiungi_stanza(nomeStanza,tipoComunicazione);
                    if(risultato){
                        System.out.println("Aggiunta avvenuta con successo");
                    }else{
                        System.out.println("ERRORE: Problemi nell'aggiunta della stanza");
                    }

                }else if(service.equals("E")){
                    String nomeUtente;
					System.out.print("Nome utente:");
					nomeUtente = stdIn.readLine();

                    Stanza[] stanzeUtente = serverRMI.elimina_utente(nomeUtente);
                    if(stanzeUtente == null){
                        System.out.println("ERRORE: utente non presente in nessuna stanza");
                    }else{
                        System.out.println("Utente rimosso dalle seguenti stanze:");
                        for(int i=0;i<stanzeUtente.length;i++){
                            System.out.println(stanzeUtente[i].getNome());
                        }
                    }

                }else{
                    System.out.println("Servizio non disponibile");
                }
                System.out.print("Servizio (A=aggiungi stanza, E=Elimina utente): ");
            }
        }
    }
    
}
