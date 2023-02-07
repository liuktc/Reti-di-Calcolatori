/*
 * LETTURA E SCRITTURA FILE
 */

FileWriter fout = new FileWriter("filename");
String res = "Ciao come stai\nHello World!";
out.write(fout,0,res.length());
fout.close();

/*
 * Filtro a caratteri
 */
FileReader r = new FileReader("filename");
char ch;
int x;
while ((x = r.read()) >= 0) { 
    ch = (char) x;
    System.out.print(ch);
}
r.close();

/*
 * --------------------SOCKET DATAGRAM---------------------------
 */

    /*----CLIENT----*/ 
        InetAddress addr = InetAddress.getByName("localhost");
        int port = 2000;
        byte[] buf = new byte[256];

        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(30000);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, port); // Specifichiamo ip e porta del server al quale vogliamo mandare il pacchetto UDP

        // Conversione del messaggio in array di byte
        ByteArrayOutputStream boStream = new ByteArrayOutputStream();
        DataOutputStream doStream = new DataOutputStream(boStream);
        doStream.writeUTF(richiesta); // Anche doStream.writeInt per scrittura tipizzata
        byte[] data = boStream.toByteArray();

        // Invio del messaggio sulla socket
        packet.setData(data);
        socket.send(packet);

        // Ricezione della risposta  del server
        byte[] buf = new byte[256];
        packet.setData(buf);
        socket.receive(packet); // catch SocketTimeoutException

        ByteArrayInputStream biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
        DataInputStream diStream = new DataInputStream(biStream);
        String risposta = diStream.readUTF(); // Anche diStream.readInt per lettura tipizzata

    /*----SERVER----*/
        byte[] buf = new byte[256];
        DatagramSocket socket = new DatagramSocket(port); // Specificando solo la porta l'indirizzo IP è il localhost
                            //  new DatagramSocket(port, address);
        DatagramPacket packet = new DatagramPacket(buf, buf.length); // non specifico IP e porta perchè non so a priori da chi mi arriva il

        // Il resto delle operazioni sono uguali a quelle del client


/*
 * --------------------SOCKET CONNESSE---------------------------
 */

    /*----CLIENT----*/
        // Apertura della connessione
        InetAddress addr = InetAddress.getByName("localhost");
                int port = 2000;
        Socket socket = new Socket(addr, port); // Crea la socket ed effettua anche la connessione
        socket.setSoTimeout(30000);
        System.out.println("Creata la socket: " + socket);
        DataInputStream inSock = new DataInputStream(socket.getInputStream());
        DataOutputStream outSock = new DataOutputStream(socket.getOutputStream());

        outSock.writeUTF(messaggio); // outSock.writeInt(numero)
        String result = inSock.readUTF();


    /*----SERVER----*/
        // Apertura della connessione
        ServerSocket serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        Socket clientSocket = null;

        // Ciclo infinito di accettazione richieste
        // Sequenziale
        while(true){
            clientSocket = serverSocket.accept(); // Aspetta una richiesta di connessione e la accetta
            DataInputStream inSock = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outSock = new DataOutputStream(clientSocket.getOutputStream());
        }
        // Concorrente
        while(true){
            clientSocket = serverSocket.accept(); // Aspetta una richiesta di connessione e la accetta
            new SlaveServerThread(clientSocket).start(); // Creazione e start di un thread riservato per servire il nuovo cliente
        }

/*
 * --------------------RMI---------------------------
 */
        /*----CLIENT----*/
            final int REGISTRYPORT = 1099;

            String registryHost = args[0];
            String serviceName = args[1];

            try {
                String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;
                ServerCongresso serverRMI = (ServerCongresso) Naming.lookup(completeName);

            }catch (NotBoundException nbe) {
                System.err.println("ClientRMI: il nome fornito non risulta registrato; " + nbe.getMessage());
                nbe.printStackTrace();
                System.exit(1);
            }

        /*----SERVER----*/
            final int REGISTRYPORT = 1099;
            String registryHost = "localhost";
            String serviceName = "ServerCongresso"; // lookup name...

            // Registrazione del servizio RMI
            String completeName = "//" + registryHost + ":" + REGISTRYPORT + "/" + serviceName;
            
            ServerCongressoImpl serverRMI = new ServerCongressoImpl();
            Naming.rebind(completeName, serverRMI);
            System.out.println("Server RMI: Servizio \"" + serviceName + "\" registrato");

/*
 * --------------------COSE VARIE---------------------------
 */
    /* Lettura direttorio */
    
    // Dopo aver scritto sul file "temp" vogliamo eliminare il file originale e sostituirlo con "temp"
    File fileorig = new File(nomeFile);
    fileorig.delete();
    
    File file = new File(nomeFile);
    File tempFile = new File("temp");       
    
    tempFile.renameTo(file);
    tempFile.delete();