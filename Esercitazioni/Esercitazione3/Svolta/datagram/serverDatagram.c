#include <stdio.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
/*****************************************/
typedef struct
{
    int op1;
    int op2;
    char tipoOp;
} Request;
/****************************************/
/* 
 * APPUNTO TROVATO SU Stackoverflow:
 * A TCP/UDP connection is identified by a tuple of five values:
 * {<protocol>, <src addr>, <src port>, <dest addr>, <dest port>}
 * Any unique combination of these values identifies a connection.
 * As a result, no two connections can have the same five values,
 * otherwise the system would not be able to distinguish these
 * connections any longer.
 * - The protocol of a socket is set when a socket is created with the socket() function.
 * - The source address and port are set with the bind() function.
 * - The destination address and port are set with the connect() function-
 */
int main(int argc, char **argv)
{
    int sd, port, len, num1, num2, ris;
    const int on = 1; // Usato per settareSO_REUSEADDR sulla socket
    struct sockaddr_in cliaddr, servaddr;
    struct hostent *clienthost;
    Request *req = (Request *)malloc(sizeof(Request));

    // Controllo argomenti
    if (argc != 2)
    {
        printf("Error: %s port\n", argv[0]);
        exit(1);
    }
    else
    {
        // Verifica intero (non fatta in questo caso ma sarebbe meglio farla)
        port = atoi(argv[1]);
        if (port < 1024 || port > 65535)
        { // Porta nel range porte disponibili
            printf("Error: %s port\n", argv[0]);
            exit(2);
        }
    }

    // Trattiamo conversioni possibili
    memset((char *)&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port = htons(port);
    sd = socket(AF_INET, SOCK_DGRAM, 0); // Creazione, bind e settaggio socket (LOCALE)

    if (sd < 0)
    {
        perror("creazione socket ");
        exit(1);
    }
    printf("Server: creata la socket, sd=%d\n", sd);

    /*
     * Primitiva per settare le opzioni di socket
     * setsockopt(sd, level, optname, &optval, optlen)
     * - sd: Socket descriptor
     * - level: Livello di protocollo per socket (Per noi sempre SOL_SOCKET)
     * - optname: Nome dell'opzione da settare
     * - &optval: Puntatore ad un area di memoria contente il valore da settare
     * - optlen: Lunghezza di optval
     */
    if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
    {
        perror("set opzioni socket ");
        exit(1);
    }

    if (bind(sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0)
    {
        perror("bind socket ");
        exit(1);
    }

    printf("Server: bind socket ok\n");
    // Ciclo infinito di ricezione e servizio
    for (;;)
    {
        len = sizeof(struct sockaddr_in);

        /*
         * In questo caso la recvfrom riempie la struttura cliaddr, in modo tale che
         * il server non debba sapere l'indirizzo del mittente in anticipo.
         * Dentro il cliaddr viene messa la struttura sockadd_in del mittente.
         */
        if (recvfrom(sd, req, sizeof(Request), 0, (struct sockaddr *)&cliaddr, &len) < 0)
        {
            perror("recvfrom ");
            continue;
        }

        // Trattiamo conversioni possibili
        num1 = ntohl(req->op1);
        num2 = ntohl(req->op2);
        printf("Operazione richiesta: %i %c %i\n", num1, req->tipoOp, num2);

        // Non usiamo gethostbyname perchè gia sappiamo il nome fisico del client
        clienthost = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
        if (clienthost == NULL)
            printf("client host not found\n");
        else
            printf("Operazione richiesta da: %s %i\n", clienthost->h_name, (unsigned)ntohs(cliaddr.sin_port));
        if (req->tipoOp == '+')
            ris = num1 + num2;
        else if (req->tipoOp == '-')
            ris = num1 - num2;
        else if (req->tipoOp == '*')
            ris = num1 * num2;
        else if (req->tipoOp == '/')
            if (num2 != 0)
                ris = num1 / num2;
            /* Risultato di default, in caso di errore.
            Sarebbe piu' corretto avere messaggi di errore, farlo per esercizio */
            else
                ris = 0;
        ris = htonl(ris);
        if (sendto(sd, &ris, sizeof(ris), 0, (struct sockaddr *)&cliaddr, len) < 0)
        {
            perror("sendto");
            continue;
        }
    } // while
} // main