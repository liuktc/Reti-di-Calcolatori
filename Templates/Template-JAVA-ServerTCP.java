/* Nome Cognome Matricola */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/* Thread lanciato per ogni richiesta accettata */ 
class Server_thread extends Thread {
    private Socket clientSocket = null;
    private Stanza[] s;

    /* Costruttore */
    public Server_thread(Socket clientSocket, Stanza[] s) {
        this.clientSocket = clientSocket;
        this.s = s;
    }

    public void run() {
        DataInputStream inSock;
        DataOutputStream outSock;
        String servizio;
        String stanza;
        try {
            /* Estrazione degli stream di I/O dalla socket */
            inSock = new DataInputStream(clientSocket.getInputStream());
            outSock = new DataOutputStream(clientSocket.getOutputStream());
            /* Lettura tipo di servizio richiesto */
            servizio = inSock.readUTF();
            if (!servizio.equals("V") && !servizio.equals("S")) {
                /* Se ricevo una richiesta che non riesco a gestire chiudo in modo anomalo */
                clientSocket.close();
                return;
            } else {
                /* Servizio di visualizzazione */
                if (servizio.equals("V")) {
                    // chiudo in input perche' non mi serve
                    clientSocket.shutdownInput();
                    // invio il numero di righe
                    outSock.writeInt(s.length);
                    // spedisco la struttura dati
                    for (int i = 0; i < s.length; i++)
                        outSock.writeUTF(s[i].toString());
                    // temrinate le scritture; chiuto anche in output
                    clientSocket.shutdownOutput();
                } 
                /* Servizio di Sospensione */
                else {
                    stanza = inSock.readUTF();
                    // chiudo in input perche' ho finito di leggere
                    clientSocket.shutdownInput();
                    outSock.writeInt(Server.sospendi(stanza));
                    // temrinate le scritture; chiuto anche in output
                    clientSocket.shutdownOutput();
                }
            }
        }
        catch (IOException ioe) {
            System.out
                    .println("Problemi nella creazione degli stream di input/output " + "su socket: ");
            ioe.printStackTrace();
            return; // Il singolo thread si termina
        } catch (Exception e) {
            System.out
                    .println("Problemi nella creazione degli stream di input/output "
                            + "su socket: ");
            e.printStackTrace();
            return; // Il singolo thread si termina
        }
    }
}

public class Server {
    private static final int N = 10;
    static Stanza s[] = null;

    public static synchronized int sospendi(String nomeStanza) {
        for (int i = 0; i < N; i++)
            if (s[i].getNome().equals(nomeStanza)) {
                if (s[i].sospendi())
                    return 0;
                else
                    return -1;
            }
        return -1;
    }

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = -1;

        /* Controllo argomenti */
        try {
            if (args.length == 1) {
                port = Integer.parseInt(args[0]);
                /* Controllo che la porta sia nel range consentito 1024-65535 */
				if (port < 1024 || port > 65535) {
					System.out.println("Usage: java Server port");
					System.exit(1);
				}
            } else {
                System.out.println("Usage: java Server port");
                System.exit(1);
            }
        }
        catch (Exception e) {
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
            System.out.println("Usage: java Server port");
            System.exit(2);
        }

        /* Inizializzazione struttura dati */
        s = new Stanza[N];
        for (int i = 0; i < N; i++) {
            s[i] = new Stanza();
        }
        s[0].setNome("Informatica");
        s[0].setStato("M");
        s[1].setNome("Ambiente");
        s[1].setStato("SM");
        s[2].setNome("Cucina");
        s[2].setStato("P");
        s[3].setNome("Motori");
        s[3].setStato("SP");
        System.out.println("Stanze inizializzate");


        /* Creazione serverSocket e settaggio reuse address */
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            System.out.println("Server: avviato ");
            System.out.println("Server: creata la server socket: " + serverSocket);
        } catch (Exception e) {
            System.err
                    .println("Server: problemi nella creazione della server socket: "
                            + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        try {
            while (true) {
                System.out.println("Server: in attesa di richieste...\n");
                try {
                    /* Bloccante finche' non avviene una connessione */
                    clientSocket = serverSocket.accept();
                    /* Timeout non necessario ma lo mettiamo per sicurezza,
                     * meglio che il server non si blocchi indefinitamente.
                     */
                    clientSocket.setSoTimeout(60000);
                    System.out.println("Server: connessione accettata: " + clientSocket);
                    /* Creazione Thread separato per la gestione della nuova connessione */
                    new Server_thread(clientSocket, s).start();
                } catch (Exception e) {
                    System.err
                            .println("Server: problemi nella accettazione della connessione: "
                                    + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            }
        }
        /* Qui catturo le eccezioni non catturate all'interno del while
         * in seguito alle quali il server termina l'esecuzione.
         */
        catch (Exception e) {
            e.printStackTrace();
            serverSocket.close();
            System.out.println("PutFileServerCon: termino...");
            System.exit(2);
        }
    }
}