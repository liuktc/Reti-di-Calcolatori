/* lw-server.c
 * Il server riceve il nome del file e, dopo aver controllato l'esistenza,
 * conta il numero dei caratteri della parola più lunga.
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
#include <unistd.h>

#define LINE_LENGTH 128

int main(int argc, char **argv) {
    int                sd;
    int                port, len, fd, nread;
    int                charCount = 0;
    const int          on        = 1;
    struct sockaddr_in cliaddr, servaddr;
    struct hostent    *clienthost;
    char               nomeFile[LINE_LENGTH];
    char               read_char, err[LINE_LENGTH];
    int                currCharCount;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 2) {
        printf("Error: %s port\n", argv[0]);
        exit(1);
    } else {
        // controllo porta
        nread = 0;
        while (argv[1][nread] != '\0') {
            if ((argv[1][nread] < '0') || (argv[1][nread] > '9')) {
                printf("Primo argomento non intero\n");
                printf("Error: %s port\n", argv[0]);
                exit(2);
            }
            nread++;
        }
        port = atoi(argv[1]);
        if (port < 1024 || port > 65535) {
            printf("Error: %s port\n", argv[0]);
            printf("1024 <= port <= 65535\n");
            exit(2);
        }
    }

    /* INIZIALIZZAZIONE INDIRIZZO SERVER ---------------------------------- */
    memset((char *)&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family      = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port        = htons(port);

    /* CREAZIONE, SETAGGIO OPZIONI E CONNESSIONE SOCKET -------------------- */
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0) {
        perror("creazione socket ");
        exit(1);
    }
    printf("Server: creata la socket, sd=%d\n", sd);

    if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket ");
        exit(1);
    }
    printf("Server: set opzioni socket ok\n");

    if (bind(sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("bind socket ");
        exit(1);
    }
    printf("Server: bind socket ok\n");

    /* CICLO DI RICEZIONE RICHIESTE ------------------------------------------ */
    for (;;) {
        len = sizeof(struct sockaddr_in);
        if (recvfrom(sd, nomeFile, sizeof(nomeFile), 0, (struct sockaddr *)&cliaddr, &len) < 0) {
            perror("recvfrom ");
            continue;
        }
        printf("Operazione richiesta per file: %s\n", nomeFile);
        clienthost = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
        if (clienthost == NULL)
            printf("client host information not found\n");
        else
            printf("Operazione richiesta da: %s %i\n", clienthost->h_name,
                   (unsigned)ntohs(cliaddr.sin_port));

        /* Verifico l'esistenza del file */
        charCount = -1;
        if ((fd = open(nomeFile, O_RDONLY)) < 0) {
            perror("open file sorgente");
        } else {
            /* ************************************** */
            // Operazione di conteggio delle parole (in linea)
            currCharCount = 0;
            while ((nread = read(fd, &read_char, sizeof(char))) != 0) {
                if (nread < 0) {
                    sprintf(err, "(PID %d) impossibile leggere dal file", getpid());
                    perror(err);
                    exit(0);
                } else {
                    if (read_char == ' ' || read_char == '\r' || read_char == '\n')
                    { // separatore: è finita una parola
                        if (currCharCount > charCount) {
                            charCount = currCharCount;
                        }
                        currCharCount = 0;
                    } else { // carattere
                        currCharCount++;
                    }
                }
            }
            /* ************************************** */
            // Chiudo il file descriptor
            close(fd);
        }

        printf("Risposta, caratteri: %d\n", charCount);
        if (sendto(sd, &charCount, sizeof(charCount), 0, (struct sockaddr *)&cliaddr, len) < 0) {
            perror("sendto ");
            continue;
        }
    } /* ciclo for */
}