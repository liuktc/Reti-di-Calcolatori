/* Client per richiedere l'invio di un file (get, versione 1) */

#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define DIM_BUFF         1000   

int main(int argc, char *argv[]) {
    int                sd, nread, nwrite, port;
    char               buff[DIM_BUFF];
    struct hostent    *host;
    struct sockaddr_in servaddr;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 3) {
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(1);
    }
    printf("Client avviato\n");

    /* PREPARAZIONE INDIRIZZO SERVER ----------------------------- */
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

    /* CORPO DEL CLIENT: */
    /* ciclo di accettazione di richieste di file ------- */
    printf("Inserire qualsiasi cosa per visualizzare lo stato (Ctrl+D per terminare): ");

    while (gets(buff)) {

        /* CREAZIONE E CONNESSIONE SOCKET (BIND IMPLICITA) ----------------- */
        sd = socket(AF_INET, SOCK_STREAM, 0);
        if (sd < 0) {
            perror("apertura socket ");
            exit(3);
        }
        printf("Creata la socket sd=%d\n", sd);

        if (connect(sd, (struct sockaddr *)&servaddr, sizeof(struct sockaddr)) < 0) {
            perror("Errore in connect");
            exit(4);
        }
        printf("Connect ok\n");
        shutdown(sd, SHUT_WR); // stop sending data

        printf("Inizio a scrivere\n");
        while ((nread = read(sd, buff, sizeof(buff))) > 0) {
            write(1,buff,nread);
        }
        printf("Chiudo connessione\n");
        shutdown(sd, SHUT_RD);

        close(sd);
        printf("Inserire qualsiasi cosa per visualizzare lo stato (Ctrl+D per terminare): ");

    } // while
    printf("\nClient: termino...\n");
    exit(0);
}
