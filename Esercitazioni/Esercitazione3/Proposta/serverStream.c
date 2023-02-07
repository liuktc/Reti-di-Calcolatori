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
#define DIM_BUFF 256

int main(int argc, char** argv){
    int                listen_sd, conn_sd;
    int                port, len,nread, num, numRiga;
    const int          on = 1;
    char buff[DIM_BUFF];
    struct sockaddr_in cliaddr, servaddr;
    struct hostent    *host;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 2) {
        printf("Error: %s port\n", argv[0]);
        exit(1);
    } else {
        num = 0;
        while (argv[1][num] != '\0') {
            if ((argv[1][num] < '0') || (argv[1][num] > '9')) {
                printf("Secondo argomento non intero\n");
                exit(2);
            }
            num++;
        }
        port = atoi(argv[1]);
        if (port < 1024 || port > 65535) {
            printf("Error: %s port\n", argv[0]);
            printf("1024 <= port <= 65535\n");
            exit(2);
        }
    }

    /* INIZIALIZZAZIONE INDIRIZZO SERVER ----------------------------------------- */
    memset((char *)&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family      = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port        = htons(port);

    /* CREAZIONE E SETTAGGI SOCKET D'ASCOLTO --------------------------------------- */
    listen_sd = socket(AF_INET, SOCK_STREAM, 0);
    if (listen_sd < 0) {
        perror("creazione socket ");
        exit(1);
    }
    printf("Server: creata la socket d'ascolto per le richieste di ordinamento, fd=%d\n",
           listen_sd);

    if (setsockopt(listen_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket d'ascolto");
        exit(1);
    }
    printf("Server: set opzioni socket d'ascolto ok\n");

    if (bind(listen_sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("bind socket d'ascolto");
        exit(1);
    }
    printf("Server: bind socket d'ascolto ok\n");

    if (listen(listen_sd, 5) < 0) // creazione coda d'ascolto
    {
        perror("listen");
        exit(1);
    }
    printf("Server: listen ok\n");

    /* CICLO DI RICEZIONE RICHIESTE --------------------------------------------- */
    for(;;){
        len = sizeof(cliaddr);
        if ((conn_sd = accept(listen_sd, (struct sockaddr*)&cliaddr, &len)) < 0) {
            /* La accept puo' essere interrotta dai segnali inviati dai figli alla loro
             * teminazione. Tale situazione va gestita opportunamente. Vedere nel man a cosa
             * corrisponde la costante EINTR!*/
            if (errno == EINTR) {
                perror("Forzo la continuazione della accept");
                continue;
            } else
                exit(1);
        }

        host = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
        if (host == NULL) {
            printf("client host information not found\n");
            continue;
        }else{
            printf("Server : host client e' %s \n", host->h_name);
        }
        printf("Server : ricevo numero riga\n");

        if(read(conn_sd,&numRiga,sizeof(numRiga))<0){
            perror("read(conn_sd)");
        }

        int cont_riga = 0;
        int righe_lette = 0;
        char riga[1024];
        // Leggo il contenuto del file, 
        while((nread = read(conn_sd,buff,DIM_BUFF)) > 0){
            //write(1,buff,nread);
            for(int i=0;i<nread;i++){
                if(buff[i] != '\n' && buff[i] != EOF){
                    riga[cont_riga] = buff[i];
                    cont_riga++;
                }else{
                    if(buff[i] == EOF){
                        printf("Ho incontrato EOF\n");
                    }
                    // Quando raggiungo un fine linea
                    // Se è la linea da eliminare la ignoro
                    // altrimenti la invio
                    righe_lette++;
                    riga[cont_riga] = '\n'; // Aggiungo lo '\n'
                    riga[cont_riga+1] = '\0'; // Aggiungo il carattere terminatore
                    
                    // Invio la riga solo se non è quella da eliminare
                    if(righe_lette != numRiga){ 
                        write(conn_sd,riga,cont_riga+1); // Invio la rig
                        write(1,riga,cont_riga + 1);
                        //printf("\n");
                    }

                    cont_riga = 0;
                }
            }
        }
        printf("Server: Fine invio della risposta");
        close(conn_sd);
    }
}