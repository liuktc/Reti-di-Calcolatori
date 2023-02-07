/* dl_server.c
 * Il server riceve numero linea e contenuto file
 * invia il contenuto del file senza la linea
 * senza salvare il file in locale.
 */

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

#define DIM_DIR     80
#define LINE_LENGTH 128
/********************************************************/
void gestore(int signo) {
    int stato;
    printf("esecuzione gestore di SIGCHLD\n");
    wait(&stato);
    printf("stato figlio: %d\n", stato >> 8);
}

/********************************************************/

int main(int argc, char **argv) {
    int                listen_sd, conn_sd, nread;
    int                port, len, line, lineCount;
    char               ccar, err[LINE_LENGTH];
    const int          on = 1;
    struct sockaddr_in cliaddr, servaddr;
    struct hostent    *host;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 2) {
        printf("Error: %s port\n", argv[0]);
        exit(1);
    } else {
        nread = 0;
        while (argv[1][nread] != '\0') {
            if ((argv[1][nread] < '0') || (argv[1][nread] > '9')) {
                printf("Terzo argomento non intero\n");
                exit(2);
            }
            nread++;
        }
        port = atoi(argv[1]);
        if (port < 1024 || port > 65535) {
            printf("Porta scorretta...");
            exit(2);
        }
    }

    /* INIZIALIZZAZIONE INDIRIZZO SERVER ----------------------------------------- */
    memset((char *)&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family      = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port        = htons(port);

    /* CREAZIONE E SETTAGGI SOCKET D'ASCOLTO --------------------------------------- */
    listen_sd = socket(AF_INET, SOCK_STREAM, 0);
    if (listen_sd < 0) {
        perror("creazione socket ");
        exit(1);
    }
    printf("Server: creata la socket d'ascolto per le richieste di ordinamento, fd=%d\n",
           listen_sd);

    if (setsockopt(listen_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket d'ascolto");
        exit(1);
    }
    printf("Server: set opzioni socket d'ascolto ok\n");

    if (bind(listen_sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("bind socket d'ascolto");
        exit(1);
    }
    printf("Server: bind socket d'ascolto ok\n");

    if (listen(listen_sd, 5) < 0) {
        perror("listen");
        exit(1);
    }
    printf("Server: listen ok\n");

    /* AGGANCIO GESTORE PER EVITARE FIGLI ZOMBIE,
     * Quali altre primitive potrei usare? E' portabile su tutti i sistemi?
     * Pregi/Difetti?
     * Alcune risposte le potete trovare nel materiale aggiuntivo!
     */
    signal(SIGCHLD, gestore);

    /* CICLO DI RICEZIONE RICHIESTE --------------------------------------------- */
    for (;;) {
        len = sizeof(cliaddr);
        if ((conn_sd = accept(listen_sd, (struct sockaddr *)&cliaddr, &len)) < 0) {
            if (errno == EINTR) {
                perror("Forzo la continuazione della accept");
                continue;
            } else
                exit(1);
        }

        if (fork() == 0) { // figlio
            close(listen_sd);
            host = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
            if (host == NULL) {
                printf("client host information not found\n");
                close(conn_sd);
                exit(2);
            } else {
                printf("Server (figlio): host client e' %s \n", host->h_name);
            }

            // Leggo il numero della linea
            if (nread = read(conn_sd, &line, sizeof(int)) <= 0) {
                sprintf(err, "(PID %d) impossibile leggere il numero di linea", getpid());
                perror(err);
                exit(EXIT_FAILURE);
            }
            printf("Server (figlio), linea: %d\n", line);

            // Lettura del file a carattere fino a EOF
            lineCount = 1;
            while ((nread = read(conn_sd, &ccar, sizeof(char))) > 0) {
                /* CORPO DEL FILTRO E SOSTITUZIONE (online: no funzione separata)*/
                if (ccar == '\n') { // finita una linea
                    if (lineCount != line)
                    { // se è la linea da saltare, non trasmetto nemmeno il newline
                        write(conn_sd, &ccar, 1);
                    }
                    lineCount = lineCount + 1;
                } else {
                    if (lineCount != line) { // non trasmetto la linea richiesta
                        write(conn_sd, &ccar, 1);
                    }
                }
            }
            // Chiudo la socket
            close(conn_sd);
        } // figlio

        // Il padre chiude socket di connessione, non quella di ascolto
        close(conn_sd);

    } // ciclo for infinito
}
