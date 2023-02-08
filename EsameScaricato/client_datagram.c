//S0000971128, Dominici, Leonardo
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

#define LINE_LENGTH 256

typedef struct
{
    int op1;
    int op2;
    char operazione;
} Request;

// Chiamta attesa: ./Client <hostname> <port>

int main(int argc, char *argv[])
{

    struct hostent *host;
    struct sockaddr_in clientaddr, servaddr;
    int port, sock, len;
    char filename[LINE_LENGTH];
    char serialNum[7];
    char toSend[LINE_LENGTH+7];
    int res;
    Request req;

    if (argc != 1)
    {
        printf("Error: %s serverAddress serverPort\n", argv[0]);
        exit(1);
    }

    port = 9000;
    if (port == 0)
    {
        printf("Error: %s serverAddress serverPort\n", argv[0]);
        exit(1);
    }
    else if (port < 1024 || port > 65535)
    {
        printf("Error:la porta deve essere fra 1024 e 65535t\n");
        exit(1);
    }

    // Init di clientaddr
    memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
    clientaddr.sin_family = AF_INET;
    clientaddr.sin_addr.s_addr = INADDR_ANY;
    clientaddr.sin_port = 0;

    // Init di serveraddr
    servaddr.sin_family = AF_INET;
    host = gethostbyname("localhost");

    if (host == NULL)
    {
        printf("Host not found...");
        exit(2);
    }

    // Setup server data
    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    servaddr.sin_port = htons(port);

    // socket creation
    sock = socket(AF_INET, SOCK_DGRAM, 0);
    if (sock < 0)
    {
        perror("Errore in fase apertura socket.");
        exit(3);
    }

    // Bind di socket con server
    if (bind(sock, (struct sockaddr *)&clientaddr, sizeof(clientaddr)) < 0)
    {
        perror("Errore in fase bind.");
        exit(3);
    }

    // Socket connessa inizio la parte di logica
    printf("Inserisci seriale oppure EOF:\n");
    while (gets(serialNum) != 0)
    {
        
        printf("Ora inserisci il nome del file da cancellare:\n");
        gets(filename);
        strcpy(toSend, serialNum);
        strcat(toSend, "|");
        strcat(toSend, filename);

        printf("Mi appreso a mandare datagramma con %s\n", toSend);
        // Invio richiesta
        len = sizeof(servaddr);
        if (sendto(sock, toSend, strlen(toSend)+1, 0, (struct sockaddr *)&servaddr, len) < 0)
        {
            perror("sendto");
            continue;
        }

        // Ricevo risultato
        printf("Attendo il risultato...\n");
        if (recvfrom(sock, &res, sizeof(int), 0, (struct sockaddr *)&servaddr, &len) < 0)
        {
            perror("recvfrom");
            continue;
        }

        printf("Risultato dal server: %d\n", res);

        printf("Inserisci seriale oppure EOF:\n");
    }

    close(sock);
    printf("Termino questo client");
    exit(0);
}