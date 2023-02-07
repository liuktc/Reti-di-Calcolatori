/* Server Select
 * 	Un solo figlio per tutti i file.
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

#define DIM_BUFF         100
#define LENGTH_FILE_NAME 20
#define LENGTH_PAROLA    20
#define max(a, b)        ((a) > (b) ? (a) : (b))
#define TEMP_FILE_NAME "tempFile"

typedef struct {
    char nome_file[LENGTH_FILE_NAME];
    char parola[LENGTH_PAROLA];
} DatagramRequest;

/********************************************************/
int elimina_parola(char *nome_file, char* parola_da_eliminare) {
    char c;
    char parola[LENGTH_PAROLA];
    int fd,cont=0,cont_parola = 0,fd_temp;
    printf("Leggo il file\n");
    if((fd = open(nome_file,O_RDONLY)) < 0){
        perror("open(nome_file)");
        return -1;
    }
    printf("Creo il file temp\n");
    if((fd_temp = open(TEMP_FILE_NAME,O_WRONLY | O_CREAT,0777)) < 0){
        perror("open(temp_file)");
        return -1;
    }

    printf("Inizio a leggere il file\n");
    while(read(fd,&c,1) > 0){
        //write(1,&c,1);
        if(c == ' ' || c == '\n'){
            //parola[cont_parola] = c;
            parola[cont_parola] = '\0';
            if(strcmp(parola,parola_da_eliminare) == 0){
                // Ho trovato la parola da eliminare, non la stampo
                cont++;
            }else{
                printf("Stampo su file: ");
                // La posso stampare nel file temporaneo
                parola[cont_parola] = c;
                parola[cont_parola+1] = '\0';
                write(fd_temp,parola,strlen(parola));
                //write(1,parola,strlen(parola));
                printf("'%s'",parola);
                printf("\n");
            }
            cont_parola = 0;
        }else{
            parola[cont_parola] = c;
            cont_parola++;
        }
    }
    //parola[cont_parola] = c;
    parola[cont_parola] = '\0';
    if(strcmp(parola,parola_da_eliminare) == 0){
        // Ho trovato la parola da eliminare, non la stampo
        cont++;
    }else{
        // La posso stampare nel file temporaneo
        write(fd_temp,parola,strlen(parola));
        printf("'%s'",parola);
        //write(1,parola,strlen(parola));
    }

    // Dopo questo ciclo il file temp conterrà il nuovo file
    // Rimuovo l'originale e rinomino il temp
    if(remove(nome_file) != 0){
        perror("delete(nome_file)");
    }
    if(rename(TEMP_FILE_NAME,nome_file) != 0){
        perror("rename(tempFile,nome_file)");
    }
    close(fd_temp);
    return cont;
}
/********************************************************/
void gestore(int signo) {
    int stato;
    printf("esecuzione gestore di SIGCHLD\n");
    wait(&stato);
}
/********************************************************/

int main(int argc, char **argv) {
    int       listenfd, connfd, udpfd, fd_file, nready, maxfdp1;
    const int on   = 1;
    char      zero = 0, buff[DIM_BUFF], nome_file[LENGTH_FILE_NAME], nome_dir[LENGTH_FILE_NAME];
    fd_set    rset;
    int       len, nread, nwrite, num, port;
    DatagramRequest req;
    struct sockaddr_in cliaddr, servaddr;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 2) {
        printf("Error: %s port\n", argv[0]);
        exit(1);
    }
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

    /* INIZIALIZZAZIONE INDIRIZZO SERVER ----------------------------------------- */
    memset((char *)&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family      = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port        = htons(port);

    printf("Server avviato\n");

    /* CREAZIONE SOCKET TCP ------------------------------------------------------ */
    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    if (listenfd < 0) {
        perror("apertura socket TCP ");
        exit(1);
    }
    printf("Creata la socket TCP d'ascolto, fd=%d\n", listenfd);

    if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket TCP");
        exit(2);
    }
    printf("Set opzioni socket TCP ok\n");

    if (bind(listenfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("bind socket TCP");
        exit(3);
    }
    printf("Bind socket TCP ok\n");

    if (listen(listenfd, 5) < 0) {
        perror("listen");
        exit(4);
    }
    printf("Listen ok\n");

    /* CREAZIONE SOCKET UDP ------------------------------------------------ */
    udpfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (udpfd < 0) {
        perror("apertura socket UDP");
        exit(5);
    }
    printf("Creata la socket UDP, fd=%d\n", udpfd);

    if (setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket UDP");
        exit(6);
    }
    printf("Set opzioni socket UDP ok\n");

    if (bind(udpfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("bind socket UDP");
        exit(7);
    }
    printf("Bind socket UDP ok\n");

    /* AGGANCIO GESTORE PER EVITARE FIGLI ZOMBIE -------------------------------- */
    signal(SIGCHLD, gestore);

    /* PULIZIA E SETTAGGIO MASCHERA DEI FILE DESCRIPTOR ------------------------- */
    FD_ZERO(&rset);
    maxfdp1 = max(listenfd, udpfd) + 1;

    /* CICLO DI RICEZIONE EVENTI DALLA SELECT ----------------------------------- */
    for (;;) {
        FD_SET(listenfd, &rset);
        FD_SET(udpfd, &rset);

        if ((nready = select(maxfdp1, &rset, NULL, NULL, NULL)) < 0) {
            if (errno == EINTR)
                continue;
            else {
                perror("select");
                exit(8);
            }
        }

        /* GESTIONE RICHIESTE SU SOCKET STREAM ------------------------------------- */
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

            if (fork() == 0) { /* processo figlio che serve la richiesta di operazione */
                close(listenfd);
                printf("Dentro il figlio, pid=%i\n", getpid());

                for (;;) {
                    if ((read(connfd, &nome_file, sizeof(nome_file))) <= 0) {
                        perror("read");
                        break;
                    }
                    printf("Richiesto file %s\n", nome_file);

                    fd_file = open(nome_file, O_RDONLY);
                    if (fd_file < 0) {
                        printf("File inesistente\n");
                        write(connfd, "N", 1);
                    } else {
                        write(connfd, "S", 1);

                        /* lettura dal file (a blocchi) e scrittura sulla socket */
                        printf("Leggo e invio il file richiesto\n");
                        while ((nread = read(fd_file, buff, sizeof(buff))) > 0) {
                            if ((nwrite = write(connfd, buff, nread)) < 0) {
                                perror("write");
                                break;
                            }
                        }
                        printf("Terminato invio file\n");

                        /* invio al client segnale di terminazione: zero binario */
                        write(connfd, &zero, 1);
                        close(fd_file);
                    } // else
                }     // for
                printf("Figlio %i: chiudo connessione e termino\n", getpid());
                close(connfd);
                exit(0);
            } // figlio
            /* padre chiude la socket dell'operazione */
            close(connfd);
        } /* fine gestione richieste di file */

        /* GESTIONE RICHIESTE SU SOCKET DATAGRAM ------------------------------------------ */
        if (FD_ISSET(udpfd, &rset)) {
            printf("Server: ricevuta richiesta di eliminare parole da file");
            len = sizeof(struct sockaddr_in);
            if(recvfrom(udpfd, &req, sizeof(req),0 ,(struct sockaddr *)&cliaddr, &len) < 0){
                perror("recvfrom");
                continue;
            }

            printf("Richiesta eliminazione della parola '%s' dal file '%s'\n", req.parola,req.nome_file);
            num = elimina_parola(req.nome_file,req.parola);
            printf("Risultato del conteggio: %i\n", num);

            /*printf("Server: ricevuta richiesta di conteggio file\n");
            len = sizeof(struct sockaddr_in);
            if (recvfrom(udpfd, &nome_dir, sizeof(nome_dir), 0, (struct sockaddr *)&cliaddr, &len) <
                0) {
                perror("recvfrom");
                continue;
            }

            printf("Richiesto conteggio dei file in %s\n", nome_dir);
            num = conta_file(nome_dir);
            printf("Risultato del conteggio: %i\n", num);*/

            /*
             * Cosa accade se non commentiamo le righe di codice qui sotto?
             * Cambia, dal punto di vista del tempo di attesa del client,
             * l'ordine col quale serviamo le due possibili richieste?
             * Cosa cambia se utilizziamo questa realizzazione, piuttosto
             * che la seconda?
             *
             */
            /*
            printf("Inizio dummy cicle\n");
            int i;
            for(i=0; i<100000; i++){ printf("Scrivo\n"); }
            printf("Fine dummy cicle\n");
            */

            if (sendto(udpfd, &num, sizeof(num), 0, (struct sockaddr *)&cliaddr, len) < 0) {
                perror("sendto");
                continue;
            }

        } /* fine gestione richieste di conteggio */
    }     /* ciclo for della select */
}
