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
#define DIM_BUFF 256
int main(int argc, char *argv[])
{
    int sd, fd_sorg, fd_dest, nread;
    char buff[DIM_BUFF];
    // FILENAME_MAX: lunghezza massima nome file. Costante di sistema.
    char nome_sorg[FILENAME_MAX + 1], nome_dest[FILENAME_MAX + 1];
    struct hostent *host;
    struct sockaddr_in servaddr;
    // Controllo argomenti
    if (argc != 3)
    {
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(1);
    }
    // Inizializzazione indirizzo server
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    host = gethostbyname(argv[1]);
    if (host == NULL)
    {
        printf("%s not found in /etc/hosts\n", argv[1]);
        exit(1);
    }
    // Verifica intero ...
    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    servaddr.sin_port = htons(atoi(argv[2]));
    // Corpo del client
    printf("Ciclo di richieste di ordinamento fino a EOF\n");
    printf("Nome del file da ordinare, EOF per terminare: ");
    /* ATTENZIONE!! Cosa accade se la riga e' piu' lunga di FILENAME_MAX?
     * Stesso problema per ogni gets. Come si potrebbe risolvere il problema? */
    //while (fgets(nome_sorg,FILENAME_MAX+1,stdin))
    while(scanf("%s",nome_sorg) > 0)
    {
        printf("File da aprire: __%s__\n", nome_sorg);
        if ((fd_sorg = open(nome_sorg, O_RDONLY)) < 0)
        {
            perror("open"); // in caso che il file da ordinare non esista
            printf("Qualsiasi tasto per procedere, EOF per fine: ");
            continue;
        }
        printf("Nome del file ordinato: ");
        if (scanf("%s",nome_dest) == 0)
            break; // Creazione file ordinato
        if ((fd_dest = open(nome_dest, O_WRONLY | O_CREAT, 0644)) < 0)
        {
            perror("open");
            printf("Qualsiasi tasto per procedere, EOF per fine:” ");
            continue;
        }
        sd = socket(AF_INET, SOCK_STREAM, 0); // Creazione socket
        if (sd < 0)
        {
            perror("apertura socket");
            exit(1);
        }
        printf("Client: creata la socket sd=%d\n", sd);
        /*
         * La connect fà implicitamente la primitiva di bind()
         * La connect è locale (non c'entra con l'accept del destinatario)
         */
        if (connect(sd, (struct sockaddr *)&servaddr,
                    sizeof(struct sockaddr)) < 0)
        {
            perror("connect");
            exit(1);
        }
        // BIND implicita e controllo di errore possibile per ogni primitiva
        // Invio e ricezione file
        while ((nread = read(fd_sorg, buff, DIM_BUFF)) > 0)
        {
            write(1, buff, nread);  // Stampa su console
            write(sd, buff, nread); // Invio
        }
        // Dall'altra parte sa che il file è finito perchè io faccio la shutdown
        shutdown(sd, 1); // 1 = SHUT_WR -> No more send
        while ((nread = read(sd, buff, DIM_BUFF)) > 0)
        {
            write(fd_dest, buff, nread);
            write(1, buff, nread);
        }
        shutdown(sd, 0); // 0 = SHUT_RD -> No more receive 
        close(fd_sorg);
        close(fd_dest);
        // Close socket necessarie per liberare il file descriptor
        close(sd);
        printf("Nome del file da ordinare, EOF per terminare:");
    }
    exit(0);
}