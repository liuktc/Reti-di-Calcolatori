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
#define DIM_BUFF 100
#define DIM_NOME 256

typedef struct {
    char car;
    int num_occ;
}req_udp;

void gestore(int signo) {
    int stato;
    printf("esecuzione gestore di SIGCHLD\n");
    wait(&stato);
}

int main(int argc, char **argv) {
    struct sockaddr_in cliaddr, servaddr;
    struct hostent    *hostTcp, *hostUdp;
    int nread=0, port, listen_sd, conn_sd, udp_sd, maxfdp1, len, resUDP;
    const int on = 1;
    fd_set rset;
    req_udp req;
    DIR *dir1, *dir2, *dir3;
    struct dirent *dd1, *dd2;
    FILE *file;
    char pen_c=NULL, read_char;
    int prima_minuscola, occ_righe=0;
    char buff[DIM_BUFF];
    char zero=0;
    char newdir[DIM_BUFF];
    char nomefile[DIM_NOME];
    


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
            if (recvfrom(udp_sd, &req, sizeof(req), 0, (struct sockaddr_in *)&cliaddr, &len) < 0) {
                perror("recvfrom ");
                continue;
            }
            printf("Operazione richiesta con il carattere: %c da cercare: %d num di volte\n", req.car, req.num_occ);

            hostUdp = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
            if (hostUdp == NULL) {
                printf("client host information not found\n");
            } else {
                printf("Operazione richiesta da: %s %i\n", hostUdp->h_name,
                       (unsigned)ntohs(cliaddr.sin_port));
            }
            resUDP = 0;
            if((dir1=opendir("."))!=NULL){
                
                while((dd1 = readdir(dir1)) != NULL){
                    if((file= open(dd1->d_name, O_RDONLY))<0){
                        resUDP=-1;
                        break;
                    }else{
                        pen_c=NULL;
                        prima_minuscola=0;
                        occ_righe=0;
                        while (nread = read(file, &read_char, sizeof(char))) /* Fino ad EOF*/ {
                            if (nread > 0){
                                if(pen_c==NULL || pen_c=='\n'){
                                    if(read_char>="a" && read_char<="z"){
                                        prima_minuscola=1;
                                    }else{
                                        prima_minuscola=0;
                                    }
                                }
                                if(read_char==req.car){
                                    occ_righe++;
                                }
                                if(read_char=='\n'){
                                    if(occ_righe==req.num_occ && prima_minuscola==1){
                                        resUDP++;
                                    }
                                    occ_righe=0;
                                }
                                pen_c=read_char;
                            }
                            else {
                                printf("Impossibile leggere dal file %s", file_in);
                                perror("Errore in lettura");
                                close(fd);
                                resUDP=-1;
                                break;
                            }
                        }

                    }
                } 
            }else{
                // err apertura dir
                resUDP=-1;
                printf("Invio risposta negativa al client per dir %s \n", dir);
                
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

                // Gestisco richiesta
                if((dir1=opendir("."))!=NULL){                
                    while((dd1 = readdir(dir1)) != NULL){
                        //e' un file
                        if((dir2=opendir(dd1->d_name))==NULL){
                            strcpy(nomefile, dd1->d_name);
                            write(conn_sd, nomefile, sizeof(nomefile));
                            file=open(dd1->d_name,O_RDONLY);
                            while((nread=read(file, buff, sizeof(buff)))>0){
                                if(write(conn_sd,buff,nread)<0){
                                    perror("write");
                                    break;
                                }
                            }
                            //invio dello zero binario
                            write(conn_sd, &zero, 1);
                        }else{
                            //e' una cartella
                            while((dd2 = readdir(dir2)) != NULL){
                                //e' un file
                                snprintf(newdir,sizeof(newdir),"%s/%s",dd1->d_name,dd2->d_name);
                                if((dir3=opendir(newdir))==NULL){
                                    strcpy(nomefile, dd2->d_name);
                                    file=open(newdir,O_RDONLY);
                                    if(file<0){
                                        perror("Open new file");
                                        break;
                                    }
                                    write(conn_sd, nomefile, sizeof(nomefile));
                                    while((nread=read(file, buff, sizeof(buff)))>0){
                                        if(write(conn_sd,buff,nread)<0){
                                            perror("write");
                                            break;
                                        }
                                    }
                                    //invio dello zero binario
                                    write(conn_sd, &zero, 1);
                                }
                            }
                        }
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