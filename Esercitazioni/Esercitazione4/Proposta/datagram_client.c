/* Client per richiedere la rimozione di una parola all'interno di un file sul server */

#include <dirent.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define LENGTH_FILE_NAME 20
#define LENGTH_PAROLA 20

typedef struct {
    char nome_file[LENGTH_FILE_NAME];
    char parola[LENGTH_PAROLA];
} DatagramRequest;

int main(int argc, char **argv) {
    struct hostent    *host;
    struct sockaddr_in clientaddr, servaddr;
    int                sd, nread, port;

    int  len, num_eliminati;
    char nome_file[LENGTH_FILE_NAME], parola[LENGTH_PAROLA];
    DatagramRequest req;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 3) {
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(1);
    }

    /* INIZIALIZZAZIONE INDIRIZZO SERVER--------------------- */
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    host                = gethostbyname(argv[1]);
    if (host == NULL) {
        printf("%s not found in /etc/hosts\n", argv[1]);
        exit(2);
    }

    nread = 0;
    while (argv[2][nread] != '\0') {
        if ((argv[2][nread] < '0') || (argv[2][nread] > '9')) {
            printf("Secondo argomento non intero\n");
            exit(2);
        }
        nread++;
    }
    port = atoi(argv[2]);
    if (port < 1024 || port > 65535) {
        printf("Porta scorretta...");
        exit(2);
    }

    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    servaddr.sin_port        = htons(port);

    /* INIZIALIZZAZIONE INDIRIZZO CLIENT--------------------- */
    memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
    clientaddr.sin_family      = AF_INET;
    clientaddr.sin_addr.s_addr = INADDR_ANY;
    clientaddr.sin_port        = 0;

    printf("Client avviato\n");

    /* CREAZIONE SOCKET ---------------------------- */
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0) {
        perror("apertura socket");
        exit(3);
    }
    printf("Creata la socket sd=%d\n", sd);

    /* BIND SOCKET, a una porta scelta dal sistema --------------- */
    if (bind(sd, (struct sockaddr *)&clientaddr, sizeof(clientaddr)) < 0) {
        perror("bind socket ");
        exit(1);
    }
    printf("Client: bind socket ok, alla porta %i\n", clientaddr.sin_port);

    /* CORPO DEL CLIENT: */
    printf("Nome del file: ");

    while (gets(req.nome_file)) {
        printf("Parola da eliminare: ");
        if(gets(req.parola) <= 0){
            perror("gets(parola)");
            printf("Nome del file: ");
            continue;
        }

        /* invio richiesta */
        printf("Invio della richiesta al server...\n");
        len = sizeof(servaddr);
        if (sendto(sd, &req, sizeof(DatagramRequest), 0, (struct sockaddr *)&servaddr, len) < 0)
        {
            perror("scrittura socket");
            printf("Nome del file: ");
            continue; // se questo invio fallisce il client torna all'inzio del ciclo
        }

        /* ricezione del risultato */
        printf("Attesa del risultato...\n");
        if (recvfrom(sd, &num_eliminati, sizeof(num_eliminati), 0, (struct sockaddr *)&servaddr, &len) < 0) {
            perror("recvfrom");
            printf("Nome del file: ");
            continue; // se questa ricezione fallisce il client torna all'inzio del ciclo
        }

        if (num_eliminati < 0)
            printf("Il file %s è scorretto o non esiste\n", nome_file);
        else
            printf("Dal file '%s' sono state eliminate %d occorrenze della parola '%s'\n", nome_file, ntohl(num_eliminati),parola);

        printf("Nome del file: ");

    } // while

    printf("\nClient: termino...\n");
    shutdown(sd, 0);
    shutdown(sd, 1);
    close(sd);
    exit(0);
}
