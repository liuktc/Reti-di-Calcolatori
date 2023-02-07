#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <regex.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#define max(a, b)        ((a) > (b) ? (a) : (b))

#define DIM_NOME 50
#define NUM_UTENTI 10
#define NUM_STANZE 20

typedef struct{
    char nomeStanza[DIM_NOME];

    char stato[3];

    char utenti[NUM_UTENTI][DIM_NOME];
} Stanza;

void gestore(int signo) {
    int stato;
    printf("esecuzione gestore di SIGCHLD\n");
    wait(&stato);
}

int main(int argc, char **argv) {
    struct sockaddr_in cliaddr, servaddr;
    struct hostent    *hostTcp, *hostUdp;
    int nread, port, listen_sd, conn_sd, udp_sd, maxfdp1, len, resUDP;
    const int on = 1;
    fd_set rset;
    Stanza stanze[NUM_STANZE];
    char nomeStanza[DIM_NOME],c,buff[(DIM_NOME*NUM_UTENTI + DIM_NOME + 3)];


    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 2) {
        printf("Error: %s port \n", argv[0]);
        exit(1);
    } else {
        nread = 0;
        while (argv[1][nread] != '\0') {
            if ((argv[1][nread] < '0') || (argv[1][nread] > '9')) {
                printf("Secondo argomento non intero\n");
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

    /* INIZIALIZZAZIONE STRUTTURA DATI -------------------------------------*/
    for(int i=0;i<NUM_STANZE;i++){
        strcpy(stanze[i].nomeStanza,"L");
        strcpy(stanze[i].stato,"L");
        for(int j=0;j<NUM_UTENTI;j++){
            strcpy(stanze[i].utenti[j],"L");
        }
    }

    /* INIZIALIZZAZIONE INDIRIZZO SERVER ----------------------------------------- */
    memset((char *)&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family      = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port        = htons(port);

    /* CREAZIONE E SETTAGGI SOCKET TCP --------------------------------------- */
    listen_sd = socket(AF_INET, SOCK_STREAM, 0);
    if (listen_sd < 0) {
        perror("creazione socket ");
        exit(3);
    }
    printf("Server: creata la socket d'ascolto per le richieste di ordinamento, fd=%d\n",
           listen_sd);

    if (setsockopt(listen_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket d'ascolto");
        exit(3);
    }
    printf("Server: set opzioni socket d'ascolto ok\n");

    if (bind(listen_sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("bind socket d'ascolto");
        exit(3);
    }
    printf("Server: bind socket d'ascolto ok\n");

    if (listen(listen_sd, 5) < 0) {
        perror("listen");
        exit(3);
    }
    printf("Server: listen ok\n");

    /* CREAZIONE E SETTAGGI SOCKET UDP --------------------------------------- */
    udp_sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (udp_sd < 0) {
        perror("apertura socket UDP");
        exit(4);
    }
    printf("Creata la socket UDP, fd=%d\n", udp_sd);

    if (setsockopt(udp_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket UDP");
        exit(4);
    }
    printf("Set opzioni socket UDP ok\n");

    if (bind(udp_sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("bind socket UDP");
        exit(4);
    }
    printf("Bind socket UDP ok\n");

    signal(SIGCHLD, gestore);

    /* PULIZIA E SETTAGGIO MASCHERA DEI FILE DESCRIPTOR ------------------------- */
    FD_ZERO(&rset);
    maxfdp1 = max(listen_sd, udp_sd) + 1;

    /* CICLO DI RICEZIONE RICHIESTE --------------------------------------------- */
    for (;;) {
        FD_SET(listen_sd, &rset);
        FD_SET(udp_sd, &rset);

        if ((nread = select(maxfdp1, &rset, NULL, NULL, NULL)) < 0) {
            if (errno == EINTR) {
                continue;
            } else {
                perror("select");
                exit(5);
            }
        }

        if (FD_ISSET(udp_sd, &rset)) {
            printf("Ricevuta richiesta dalla socket UDP\n");

            len = sizeof(struct sockaddr_in);
            if (recvfrom(udp_sd, nomeStanza, sizeof(nomeStanza), 0, (struct sockaddr_in *)&cliaddr, &len) < 0) {
                perror("recvfrom ");
                continue;
            }
            printf("Richiesta sospensione della stanza %s",nomeStanza);

            hostUdp = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
            if (hostUdp == NULL) {
                printf("client host information not found\n");
            } else {
                printf("Operazione richiesta da: %s %i\n", hostUdp->h_name,
                       (unsigned)ntohs(cliaddr.sin_port));
            }
            resUDP = -1;
            for(int i=0;i<NUM_STANZE;i++){
                if(strcmp(stanze[i].nomeStanza,nomeStanza) == 0){
                    if(strcmp(stanze[i].stato,"SP") == 0 || strcmp(stanze[i].stato,"SM") == 0){
                        // Stanza già sospesa
                        resUDP = -1;
                    }else{
                        if(strcmp(stanze[i].stato,"P") == 0){
                            strcpy(stanze[i].stato,"SP");
                            resUDP = 0;
                        }else if(strcmp(stanze[i].stato,"M") == 0){
                            strcpy(stanze[i].stato,"SM");
                            resUDP = 0;
                        }else{
                            resUDP = -1;
                        }
                    }
                    break;
                }
            }
            // conversione in formato di rete
            resUDP=htonl(resUDP);

            if (sendto(udp_sd, &resUDP, sizeof(resUDP), 0, (struct sockaddr_in *)&cliaddr, len) < 0) {
                perror("sendto ");
                continue;
            }
        }

        if (FD_ISSET(listen_sd, &rset)) {
            printf("Ricevuta richiesta dalla socket TCP\n");

            len = sizeof(cliaddr);
            if ((conn_sd = accept(listen_sd, (struct sockaddr *)&cliaddr, &len)) < 0) {
                if (errno == EINTR) {
                    perror("Forzo la continuazione della accept");
                    continue;
                } else {
                    exit(6);
                }
            }

            // Creazione figlio
            if (fork() == 0) {
                // Chiudo listen_sd perchè non serve al figlio
                close(listen_sd);
                hostTcp = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
                if (hostTcp == NULL) {
                    printf("client host information not found\n");
                    close(conn_sd);
                    exit(6);
                } else {
                    printf("Server (figlio): host client e' %s \n", hostTcp->h_name);
                }

                shutdown(conn_sd,SHUT_RD);
                // Gestisco richiesta
                for(int i=0;i<NUM_STANZE;i++){
                    //snprintf(buff,sizeof(buff),"%s\t%s",stanze[i].nomeStanza,stanze[i].stato);
                    strcpy(buff,stanze[i].nomeStanza);
                    strcat(buff,"\t");
                    strcat(buff,stanze[i].stato);
                    for(int j=0;j<NUM_UTENTI;j++){
                        strcat(buff,"\t");
                        strcat(buff,stanze[i].utenti[j]);
                    }
                    strcat(buff,"\n");

                    // Invio del buffer al client
                    if(write(conn_sd,buff,strlen(buff))<0){
                        perror("write");
                        break;
                    }
                }

                // Libero risorse
                printf("Figlio TCP terminato, libero risorse e chiudo. \n");
                shutdown(conn_sd,SHUT_WR);

                //close(conn_sd);
                exit(0);
            }
            close(conn_sd); // Padre chiude conn_sd
        }
    }
}