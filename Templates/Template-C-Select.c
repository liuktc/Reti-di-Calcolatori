/* Nome Cognome Matricola */

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

void gestore(int signo) {
    int stato;
    printf("esecuzione gestore di SIGCHLD\n");
    wait(&stato);
}

int main(int argc, char **argv) {
    struct sockaddr_in cliaddr, servaddr;
    struct hostent    *hostTcp, *hostUdp;
    int nread, port, listen_sd, conn_sd, udp_sd, maxfdp1, len;
    const int on = 1;
    fd_set rset;


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

            // Ricezione messaggio in arrivo
            len = sizeof(struct sockaddr_in);
            /*if (recvfrom(udp_sd, nomeStanza, sizeof(nomeStanza), 0, (struct sockaddr_in *)&cliaddr, &len) < 0) {
                perror("recvfrom ");
                continue;
            }*/

            hostUdp = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
            if (hostUdp == NULL) {
                printf("client host information not found\n");
            } else {
                printf("Operazione richiesta da: %s %i\n", hostUdp->h_name,(unsigned)ntohs(cliaddr.sin_port));
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
                // Se devo avere la stessa connessione per più richieste devo mettere un while
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
                }
                // altrimento se ho una connessione per ogni richiesta posso ometterlo
                if (fork() == 0) { /* processo figlio che serve la richiesta di operazione */
                    close(listenfd);
                    printf("Dentro il figlio, pid=%i\n", getpid());
                    /* non c'e' piu' il ciclo perche' viene creato un nuovo figlio */
                    /* per ogni richiesta di file */
                    if (read(connfd, &nome_file, sizeof(nome_file)) <= 0) {
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
                        /* lettura e invio del file (a blocchi)*/
                        printf("Leggo e invio il file richiesto\n");
                        while ((nread = read(fd_file, buff, sizeof(buff))) > 0) {
                            if ((nwrite = write(connfd, buff, nread)) < 0) {
                                perror("write");
                                break;
                            }
                        }
                        printf("Terminato invio file\n");
                        /* non e' piu' necessario inviare al client un segnale di terminazione */
                        close(fd_file);
                    }

                    /*la connessione assegnata al figlio viene chiusa*/
                    printf("Figlio %i: termino\n", getpid());
                    shutdown(connfd, 0);
                    shutdown(connfd, 1);
                    close(connfd);
                    exit(0);
                }
                // Libero risorse
                printf("Figlio TCP terminato, libero risorse e chiudo. \n");
                close(conn_sd);
                exit(0);
            }
            close(conn_sd); // Padre chiude conn_sd
        }
    }
}