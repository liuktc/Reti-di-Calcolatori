/*
 * Perchè facciamo REUSE_ADDRESS nei server?
 * Perchè così quando andiamo in crash possiamo riutilizzare la stessa porta
 */
#include <stdio.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/wait.h>
/**************************/
void gestore(int signo)
{
    int stato;
    printf("esecuzione gestore di SIGCHLD\n");
    wait(&stato);
}
/**************************/
int main(int argc, char **argv)
{
    int listen_sd, conn_sd;
    int port, len;
    const int on = 1;
    struct sockaddr_in cliaddr, servaddr;
    struct hostent *host;
    if (argc != 2) // Controllo argomenti
    {
        printf("Error: %s port\n", argv[0]);
        exit(1);
    }
    else
        port = atoi(argv[1]); // Verifica intero ... Controllo porta????
    // Inizializzazione indirizzo server
    memset((char *)&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    // La porta è da girare in BigEndian con htons
    servaddr.sin_port = htons(port);
    // Creazione, bind e settaggio opzioni socket ascolto
    listen_sd = socket(AF_INET, SOCK_STREAM, 0);
    if (listen_sd < 0)
    {
        perror("creazione socket ");
        exit(1);
    }
    if (setsockopt(listen_sd, SOL_SOCKET, SO_REUSEADDR,
                   &on, sizeof(on)) < 0)
    {
        perror("...");
        exit(1);
    }
    printf("Server: set ok\n");
    if (bind(listen_sd, (struct sockaddr *)&servaddr,
             sizeof(servaddr)) < 0)
    {
        perror("bind socket d'ascolto");
        exit(1);
    }
    printf("Server: bind socket d'ascolto ok\n");
    if (listen(listen_sd, 5) < 0) // Creazione coda d’ascolto
    {
        perror("listen");
        exit(1);
    }
    /* Aggancio gestore per evitare figli zombie. Quali altre primitive potrei usare?
     * E' portabile su tutti i sistemi? Pregi/Difetti? */
    signal(SIGCHLD, gestore);
    for (;;) // Ciclo di ricezione richieste
    {
        if ((conn_sd = accept(listen_sd,
                              (struct sockaddr *)&cliaddr, &len)) < 0)
        {
            /* La accept puo’ essere interrotta dai segnali inviati dai figli alla loro teminazione.
             * Tale situazione va gestita opportunamente.
             * Vedere nel man a cosa corrisponde la costante EINTR! 
             */
            if (errno == EINTR)
            {
                perror("Forzo la continuazione della accept");
                continue;
            }
            else
                exit(1);
        }
        if (fork() == 0) // Figlio
        {
            /*
             * Si possono avere più processi sulla stessa connessione ma è un po' pericoloso,
             * quindi di solito si chiude la connessione di uno dei due processi per non fare casino
             */
            // Chiusura file descriptor non utilizzati e ridirezione di stdin e stdout
            close(listen_sd);
            close(1); // stdout
            close(0); // stdin
            /*
             * Faccendo due volte la dup, il socket descriptor delle connessione finisce sia sullo
             * stdout che sullo stdin, quindi il comando di sort prendera come input le read dalla
             * connessione e scriverà con le write al client sulla connessione
             */
            dup(conn_sd);
            dup(conn_sd);
            close(conn_sd); // Chiudo l'originale file descriptor, tipo modo d'uso del piping
            // Esecuzione ordinamento
            execl("/bin/sort", "sort", (char *)0);
        }else{
            // PADRE: chiusura socket di connessione (NON di ascolto)
            close(conn_sd);
        }
    }
}