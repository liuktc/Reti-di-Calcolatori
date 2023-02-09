import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;

public class RMI_Client {

	public static void main(String[] args) {
        final int PORT = 1099;
		String host = null;
		String serviceName = "Server";
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		// Controllo dei parametri della riga di comando
		if (args.length != 1) {
			System.out.println("Sintassi: RMI_Server ServiceName");
			System.exit(1);
		}
		host = args[0];

		System.out.println("Invio richieste a " + host + " per il servizio di nome " + serviceName);

        

		// Connessione al servizio RMI remoto
		try {
			String completeName = "//" + host + ":" + PORT + "/" + serviceName;
            /* Ottenimento del riferimento all'oggetto remoto */
			RMI_Server serverRMI = (RMI_Server) Naming.lookup(completeName);
			System.out.println("ClientRMI: Servizio \"" + serviceName + "\" connesso");

            String s;
            System.out.println("Serizio N(umerazione righe) o L(lista file): ");

            while ((s = stdIn.readLine()) != null) {

				if (s.equals("N")) {
                    System.out.println("Inserire il nome file: ");
                    String nomeFile=stdIn.readLine();

                    int occ=serverRMI.numerazione_righe(nomeFile);
                    if(occ<0){
                        System.out.println("Problemi nel contare le occorrenze");
                    }else{
                        System.out.println("Le occorrenze sono: "+occ);
                    }
                }
                else if(s.equals("L")){
                    System.out.println("Inserire il nome del direttorio: ");
                    String dir=stdIn.readLine();

                    String[] files=serverRMI.lista_file(dir);
                    if(files==null){
                        System.out.println("Direttorio inesistente!");
                    }else{
                        for(int i=0; i<files.length; i++){
                            System.out.println("Nome del file "+i+" "+files[i]);
                        }
                    }

                }else{
                    System.out.println("Servizio non riconosciuto");
                }
                System.out.println("Serizio N(umerazione righe) o L(lista file): ");
            }

        } catch (NotBoundException nbe) {
			System.err.println("ClientRMI: il nome fornito non risulta registrato; " + nbe.getMessage());
			nbe.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			System.err.println("ClientRMI: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
    }
}
