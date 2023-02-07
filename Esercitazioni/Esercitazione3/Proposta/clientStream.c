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

char *mygets(char *buf,size_t size){
    if(buf != NULL && size > 0){
        if(fgets(buf,size,stdin)){
            buf[strcspn(buf,"\n")] = '\0';
            return buf;
        }
        *buf = '\0';
    }
    return NULL;
}

int main(int argc,char ** argv){
    int  sd, port, fd_sorg, fd_dest, nread, numriga;
    char rigaletta[256];
    char buff[DIM_BUFF];
    // FILENAME_MAX: lunghezza massima nome file. Costante di sistema.
    char               nome_sorg[FILENAME_MAX + 1], nome_dest[FILENAME_MAX + 1];
    struct hostent    *host;
    struct sockaddr_in servaddr;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 3) {
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(1);
    }

    /* INIZIALIZZAZIONE INDIRIZZO SERVER -------------------------- */
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    host                = gethostbyname(argv[1]);

    /*VERIFICA INTERO*/
    nread = 0;
    while (argv[2][nread] != '\0') {
        if ((argv[2][nread] < '0') || (argv[2][nread] > '9')) {
            printf("Secondo argomento non intero\n");
            exit(2);
        }
        nread++;
    }
    port = atoi(argv[2]);

    /* VERIFICA PORT e HOST */
    if (port < 1024 || port > 65535) {
        printf("%s = porta scorretta...\n", argv[2]);
        exit(2);
    }
    if (host == NULL) {
        printf("%s not found in /etc/hosts\n", argv[1]);
        exit(2);
    } else {
        servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
        servaddr.sin_port        = htons(port);
    }

    /* CORPO DEL CLIENT:
    ciclo di accettazione di richieste da utente ------- */
    printf("Ciclo di richieste del client fino a EOF\n");
    printf("Nome del file , EOF per terminare: ");

    while (mygets(nome_sorg,sizeof(nome_sorg))) {
        printf("File da aprire: __%s__\n", nome_sorg);

        /* Verifico l'esistenza del file */
        if ((fd_sorg = open(nome_sorg, O_RDONLY)) < 0) {
            perror("open file sorgente");
            printf("Qualsiasi tasto per procedere, EOF per fine: ");
            continue;
        }
        printf("Numero di riga da eliminare:");
        if(mygets(rigaletta,sizeof(rigaletta)) == 0){
            break;
        }else{
            numriga = atoi(rigaletta);
            if(numriga == 0){
                printf("Errore nel numero di riga inserito!");
                continue;
            }
        }

        printf("Nome del file destinazione: ");
        if (mygets(nome_dest,sizeof(nome_dest)) == 0)
            break;

        /*Verifico creazione file*/
        if ((fd_dest = open(nome_dest, O_WRONLY | O_CREAT, 0644)) < 0) {
            perror("open file destinatario");
            printf("Nome del file da ordinare, EOF per terminare: ");
            continue;
        }

        /* CREAZIONE SOCKET ------------------------------------ */
        sd = socket(AF_INET, SOCK_STREAM, 0);
        if (sd < 0) {
            perror("apertura socket");
            exit(1);
        }
        printf("Client: creata la socket sd=%d\n", sd);

        /* Operazione di BIND implicita nella connect */
        if (connect(sd, (struct sockaddr *)&servaddr, sizeof(struct sockaddr)) < 0) {
            perror("connect");
            exit(1);
        }
        printf("Client: connect ok\n");
        

        // Invio del numero di riga
        printf("Client: invio il numero di riga al client");
        if(write(sd,&numriga,sizeof(numriga))<0){
            perror("write(sd)");
            continue;
        }
        printf("Client: invio il contenuto del file");
        while ((nread = read(fd_sorg, buff, DIM_BUFF)) > 0) {
            write(sd, buff, nread); // invio
        }
        printf("Client: file inviato\n");
        /* Chiusura socket in spedizione -> invio dell'EOF */
        shutdown(sd, 1);

        /*RICEZIONE File*/
        printf("Client: ricevo e stampo file modificato\n");
        while ((nread = read(sd, buff, DIM_BUFF)) > 0) {
            write(fd_dest, buff, nread);
            write(1, buff, nread);
        }
        printf("Traspefimento terminato\n");
        /* Chiusura socket in ricezione */
        shutdown(sd, 0);
        /* Chiusura file */
        close(fd_sorg);
        close(fd_dest);
        close(sd);

        printf("Nome del file, EOF per terminare: ");
    }
    printf("\nClient: termino...\n");
    exit(0);
}