/*
 * INCLUDE VARI
 */
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

/*
 * LETTURA E SCRITTURA FILE
 */

fd = open(file_out, O_WRONLY | O_CREAT | O_TRUNC, 00640);
if (fd < 0) {
    perror("P0: Impossibile creare/aprire il file");
    exit(2);
}

int written = write(fd, riga, strlen(riga)); // uso della primitiva
if (written < 0) {
    perror("P0: errore nella scrittura sul file");
    exit(3);
}

fd = open(file_in, O_RDONLY);
if (fd < 0) {
    perror("P0: Impossibile aprire il file.");
    exit(2);
}

// FILTRO A CARATTERI
while (nread = read(fd, &read_char, sizeof(char))) /* Fino ad EOF*/ {
    if (nread > 0)
        putchar(read_char); // Stampa a terminale
    else {
        printf("Impossibile leggere dal file %s", file_in);
        perror("Errore in lettura");
        close(fd);
        exit(3);
    }
}
close(fd);
    
/*
 * scanf
 * Attenzione perchè lascia un carattere spurio(fine linea) nel buffer
 * bisogna quindi fare una lettura a vuoto con la gets
 */

int readValues = scanf("%d", &righe);
if (readValues != 1) {
    printf("Errore: immettere un intero!");
    exit(1);
}
gets(buf); // consumare il fine linea

// FILE DESCRIPTORS
/*
 * stdin :  0
 * stdout : 1
 * stderr : 2
 */

/*
 * --------------------SOCKET DATAGRAM---------------------------
 */
    // Include vari
    #include <netdb.h>
    #include <netinet/in.h>
    #include <stdio.h>
    #include <stdlib.h>
    #include <string.h>
    #include <sys/socket.h>
    #include <sys/types.h>
    #include <unistd.h>
    #include <errno.h>
    #include <fcntl.h>
    #include <signal.h>

    /*----CLIENT----*/

        // Creazione address client
        struct sockaddr_in clientaddr; // Struttura che contiene IP + porta della mia socket

        memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
        clientaddr.sin_family      = AF_INET;
        clientaddr.sin_addr.s_addr = INADDR_ANY; // In questo modo accettiamo tutti i messaggi in arrivo
        /* Passando 0 ci leghiamo ad un qualsiasi indirizzo libero */
        clientaddr.sin_port = 0;

        // Creazione address server
        struct sockaddr_in serveraddr;
        struct hostent    *host;

        memset((char *)&serveraddr, 0, sizeof(struct sockaddr_in));
        serveraddr.sin_family      = AF_INET;
        host                     = gethostbyname(argv[1]); // Andiamo a leggere l'indirizzo IP del server come argomento di invocazione del programma
        serveraddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
        serveraddr.sin_port        = htons(port);

        // Creazione socket locale
        int sd = socket(AF_INET, SOCK_DGRAM, 0); // 0 mette un protocollo di default (quindi UDP o TCP)
        if (sd < 0) {
            perror("apertura socket");
            exit(1);
        }

        /* 
         * Andiamo a fare il bind della socket con l'indirizzo IP + porta locale.
         * NOTA: operazione puramente locale, NON è la connessione con il server
         */
        if (bind(sd, (struct sockaddr_in *)&clientaddr, sizeof(clientaddr)) < 0) {
            perror("bind socket ");
            exit(1);
        }

        // DEFINIZIONE RICHIESTA -> Importante perchè dobbiamo mandare tutto in un unico messaggio UDP
        typedef struct {
            int  op1;
            int  op2;
            char tipoOp;
        } Request;

        /* Invio messaggio al server */
        Request req; // Richiesta da riempire
        int len = sizeof(servaddr);
        if (sendto(sd, &req, sizeof(Request), 0, (struct sockaddr_in *)&serveraddr, len) < 0) {
            perror("sendto");
            continue;
        }

        /* Ricezione del messaggio da parte del server*/
        printf("Attesa del risultato...\n");
        int ris;
        if (recvfrom(sd, &ris, sizeof(ris), 0, (struct sockaddr_in *)&servaddr, &len) < 0) {
            perror("recvfrom");
            continue;
        }

    /*----SERVER----*/

        struct sockaddr_in cliaddr, servaddr;
        /* INIZIALIZZAZIONE INDIRIZZO SERVER ---------------------------------- */
        memset((char *)&servaddr, 0, sizeof(servaddr));
        servaddr.sin_family      = AF_INET;
        servaddr.sin_addr.s_addr = INADDR_ANY;
        servaddr.sin_port        = htons(port);

        sd = socket(AF_INET, SOCK_DGRAM, 0);
        if (sd < 0) {
            perror("creazione socket ");
            exit(1);
        }

        // Settaggio dell'opzione reuse addres
        const int on = 1;
        if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
            perror("set opzioni socket ");
            exit(1);
        }

        if (bind(sd, (struct sockaddr_in *)&servaddr, sizeof(servaddr)) < 0) {
            perror("bind socket ");
            exit(1);
        }

        // Ciclo ricezione richieste
        for(;;){
            Request *req = (Request *)malloc(sizeof(Request));
            len = sizeof(struct sockaddr_in);
            // La recvfrom riempe clientaddr con le informazione contenute nel messaggio ricevuto  (IP + porta)
            if (recvfrom(sd, req, sizeof(Request), 0, (struct sockaddr_in *)&cliaddr, &len) < 0) {
                perror("recvfrom ");
                continue;
            }

            clienthost = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
            if (clienthost == NULL)
                printf("client host information not found\n");
            else
                printf("Operazione richiesta da: %s %i\n", clienthost->h_name,
                       (unsigned)ntohs(cliaddr.sin_port));
            // Calcolo e invio della risposta
            int ris = req->qualcosa * 2;
            if (sendto(sd, &ris, sizeof(ris), 0, (struct sockaddr_in *)&cliaddr, len) < 0) {
                perror("sendto ");
                continue;
            }
        }

/*
 * --------------------SOCKET CONNESSE---------------------------
 */

    #include <dirent.h>
    #include <errno.h>
    #include <fcntl.h>
    #include <netdb.h>
    #include <netinet/in.h>
    #include <signal.h>
    #include <stdio.h>
    #include <stdlib.h>
    #include <string.h>
    #include <sys/socket.h>
    #include <sys/types.h>
    #include <sys/wait.h>
    #include <unistd.h>

    /*----CLIENT----*/

        /* CREAZIONE SOCKET ------------------------------------ */
        int sd = socket(AF_INET, SOCK_STREAM, 0);
        if (sd < 0) {
            perror("apertura socket");
            exit(1);
        }

        /* Operazione di BIND implicita nella connect */
        if (connect(sd, (struct sockaddr_in *)&servaddr, sizeof(struct sockaddr_in)) < 0) {
            perror("connect");
            exit(1);
        }

        // Apertura file
        if ((fd_sorg = open(nome_sorg, O_RDONLY)) < 0) {
            perror("open file sorgente");
        }
        // Esempio invio file
        char buff[DIM_BUFF];
        printf("Client: stampo e invio file da ordinare\n");
        while ((nread = read(fd_sorg, buff, DIM_BUFF)) > 0) {
            //write(1, buff, nread);  // stampa
            write(sd, buff, nread); // invio
        }
        /* Chiusura socket in spedizione -> invio dell'EOF */
        shutdown(sd, 1); // Chiusura output

        // Esempio ricezione file
        printf("Client: ricevo e stampo file ordinato\n");
        while ((nread = read(sd, buff, DIM_BUFF)) > 0) {
            write(fd_dest, buff, nread);
            write(1, buff, nread);
        }
        /* Chiusura socket in ricezione */
        shutdown(sd, 0);

        close(fd_sorg);
        close(fd_dest);
        close(sd);

    /*----SERVER----*/

        /* CREAZIONE E SETTAGGI SOCKET D'ASCOLTO --------------------------------------- */
        listen_sd = socket(AF_INET, SOCK_STREAM, 0);
        if (listen_sd < 0) {
            perror("creazione socket ");
            exit(1);
        }
        if (setsockopt(listen_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
            perror("set opzioni socket d'ascolto");
            exit(1);
        }
        if (bind(listen_sd, (struct sockaddr_in *)&servaddr, sizeof(servaddr)) < 0) {
            perror("bind socket d'ascolto");
            exit(1);
        }
        if (listen(listen_sd, 5) < 0) // creazione coda d'ascolto
        {
            perror("listen");
            exit(1);
        }
        /********************************************************/
        void gestore(int signo) {
            int stato;
            printf("esecuzione gestore di SIGCHLD\n");
            wait(&stato);
        }
        /********************************************************/
        /* AGGANCIO GESTORE PER EVITARE FIGLI ZOMBIE */
        signal(SIGCHLD, gestore);

        /* CICLO DI RICEZIONE RICHIESTE --------------------------------------------- */
        for (;;) {
            len = sizeof(cliaddr);
            if ((conn_sd = accept(listen_sd, (struct sockaddr_in *)&cliaddr, &len)) < 0) {
                /* La accept puo' essere interrotta dai segnali inviati dai figli alla loro
                 * teminazione.*/
                if (errno == EINTR) { // la costante EINTR significa che siamo stati interroti da un segnale
                    perror("Forzo la continuazione della accept");
                    continue;
                } else
                    exit(1);
            }

            if (fork() == 0) { // figlio
                /*Chiusura FileDescr non utilizzati e ridirezione STDIN/STDOUT*/
                close(listen_sd);
                host = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
                if (host == NULL) {
                    printf("client host information not found\n");
                    continue;
                } else
                    printf("Server (figlio): host client e' %s \n", host->h_name);
                printf("Server (figlio): eseguo l'ordinamento\n");

                close(1); // Chiudo STDOUT
                close(0); // Chiudo STDIN
                dup(conn_sd); // Ridirezione STDIN sulla socket
                dup(conn_sd); // Ridirezione STDOUT sulla socket
                close(conn_sd);
                /*execl = execute and leave, esegue un processo e termina.
                 * Prestare attenzione al path del programma SORT sull'host server.
                 * Alcuni comandi UNIX per cercare path:
                 *  which; whereis...
                 */
                execl("/usr/bin/sort", "sort", (char *)0);
            }
            close(conn_sd);
        }

/*
 * --------------------SELECT SERVER---------------------------
 */
        /* PULIZIA E SETTAGGIO MASCHERA DEI FILE DESCRIPTOR ------------------------- */
        fd_set rset;
        FD_ZERO(&rset); // Mette tutti 0 nella maschera
        maxfdp1 = max(listenfd, udpfd) + 1;

        /* CICLO DI RICEZIONE EVENTI DALLA SELECT ----------------------------------- */
        for (;;) {
            FD_SET(listenfd, &rset); // Mette 1 nella maschera
            FD_SET(udpfd, &rset);

            if ((nready = select(maxfdp1, &rset, NULL, NULL, NULL)) < 0) {
                if (errno == EINTR)
                    continue;
                else {
                    perror("select");
                    exit(8);
                }
            }

            /* GESTIONE RICHIESTE TCP ------------------------------------- */
            if (FD_ISSET(listenfd, &rset)) {
                printf("Ricevuta richiesta di get di un file\n");
                len = sizeof(struct sockaddr_in);
                if ((connfd = accept(listenfd, (struct sockaddr *)&cliaddr, &len)) < 0) {
                    if (errno == EINTR)
                        continue;
                    else {
                        perror("accept");
                        exit(9);
                    }
                }
            }


            /* GESTIONE RICHIESTE UDP ------------------------------------------ */
            if (FD_ISSET(udpfd, &rset)) {
                printf("Ricevuta richiesta di conteggio file\n");
                // recvfrom e sendto
            }
        }

/*
 * --------------------RPC---------------------------
 */
        /*----CLIENT----*/
            #include "operazioni.h"
            #include <rpc/rpc.h>

            CLIENT  *cl;
            cl = clnt_create(server, OPERAZIONIPROG, OPERAZIONIVERS, "udp");
            if (cl == NULL) {
                clnt_pcreateerror(server);
                exit(1);
            }

            int* ris = somma_1(&op,cl);

            // Classico ciclo di interazione

            printf("operazioni:  CS=Conta File maggiori di, SF=Scan File remoto\n");
            while (gets(ok)) {
                if ((strcmp(ok, "CS") != 0) && (strcmp(ok, "SF") != 0)) {
                    printf("scelta non disponibile\n");
                    printf("operazioni:  CS=Conta File maggiori di, SF=Scan File remoto\n");
                    continue;
                }

                printf("Richiesto servizio: %s\n", ok);

                // richiesta conteggio file nel direttorio remoto
                if (strcmp(ok, "CS") == 0) {
                    // Fai una operazione
                }else if (strcmp(ok,"SF") == 0){
                    // Fai un altra operazione
                }else{
                    printf("Servizio non supportato!\n");
                }
            }

        /*----SERVER----*/

            #include "operazioni.h"
            #include <rpc/rpc.h>

            // Bisogna sempre ritornare un puntatore
            int *somma_1_svc(Operandi *op, struct svc_req *rp) {
                /*----------------------------------------*/
                /*IL VALORE DI RITORNO DEVE ESSERE STATICO*/
                /*----------------------------------------*/
                static int ris; 
                printf("Operandi ricevuti: %i e %i\n", op->op1, op->op2);
                ris = (op->op1) + (op->op2);
                printf("Somma: %i\n", ris);
                return (&ris);
            }


            int *moltiplicazione_1_svc(Operandi *op, struct svc_req *rp) {
                
            }

        /*----FILE.X----*/
            // Attenzione: non si possono definire le matrici
            // fare una struttura riga e poi una vettore di righe;
            struct Utente{
                char nome[DIM_NOME];
            };

            struct Stanza{
                char nomeStanza[DIM_NOME];
                char tipo[3];
                Utente utenti[NUM_UTENTI]; // Matrice di nomi   
            };


            struct Operandi{
                int op1;
                int op2;
            };

            program OPERAZIONIPROG {
                version OPERAZIONIVERS {
                    int SOMMA(Operandi) = 1;
                    int MOLTIPLICAZIONE(Operandi) = 2;
                } = 1;
            } = 0x20000013;

/*
 * --------------------COSE VARIE---------------------------
 */
    /*Lettura intero compreso fra 0 e NUMFILE-1*/
    int fila = -1;
    char c,ok[256];
    while (fila < 0 || fila > (NUMFILE - 1)) {
        printf("Inserisci la fila (da 0 a %i): \n", (NUMFILE - 1));
        while (scanf("%d", &fila) != 1) {
            do {
                c = getchar();
                printf("%c ", c);
            } while (c != '\n');
            printf("Fila: ");
        }
    }
    // Consumo il fine linea
    gets(ok);


    /*Bubble sort*/
    // Ordino res in base al punteggio (Bubble sort)
    for (i = 0; i < NUM_GIUDICI - 1; i++) {
        for (k = 0; k < NUM_GIUDICI - i - 1; k++) {
             /* For decreasing order use '<' instead of '>' */
            if (res.classificaGiudici[k].punteggioTot < res.classificaGiudici[k + 1].punteggioTot)
            {
                swap                         = res.classificaGiudici[k];
                res.classificaGiudici[k]     = res.classificaGiudici[k + 1];
                res.classificaGiudici[k + 1] = swap;
            }
        }
    }

    /* Leggere i file all'interno di un direttorio */
    DIR *dir1, *dir2, *dir3;
    struct dirent *dd1, *dd2;
    char dir[DIR_LEN], newDir[DIR_LEN];

    if ((dir1 = opendir(dir)) != NULL) {
        while ((dd1 = readdir(dir1)) != NULL) {
            // Ignoro le cartelle speciali . e ..
            if (strcmp(dd1->d_name, ".") != 0 && strcmp(dd1->d_name, "..") != 0) {
                snprintf(newDir,sizeof(newDir),"%s/%s",dir,dd1->d_name);

                if((dir2 = opendir(newDir)) != NULL){
                    // dd1 è una cartella
                    while((dd2 = readdir(dir2)) != NULL){
                        // Ignoro le cartelle speciali . e ..
                        if (strcmp(dd2->d_name, ".") != 0 && strcmp(dd2->d_name, "..") != 0) {
                            snprintf(newDir,sizeof(newDir),"%s/%s/%s",dir,dd1->d_name,dd2->d_name);
                            if((dir3 = opendir(newDir)) != NULL){
                                // dd2 è una cartella
                            }else{
                                // dd2 è un file
                            }
                        }
                    }
                }else{
                    // dd1 è un file
                }
            }
        }
    }

    /* Controllare se un file è un file di testo */
    int len = strlen(dd1->d_name);
    int isTextFile;
    if(len < 4){
        isTextFile = 0;
    }else{
        isTextFile = (dd1->d_name[len-4] == '.' &&
                      dd1->d_name[len-3] == 't' &&
                      dd1->d_name[len-2] == 'x' &&
                      dd1->d_name[len-1] == 't');
    }
    
