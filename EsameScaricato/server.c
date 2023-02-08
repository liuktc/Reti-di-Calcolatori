//S0000971128, Dominici, Leonardo
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
#include <dirent.h>

#define N 5
#define BUFFER_SIZE 256

struct linea_struct{
    char id[8];
    int scadenza[3];    //GG/MM/AAAA
    char brand[8];
    char folderName[32];
};

// In caso in cui il main thread generi dei figli, poi ricordati di fare signal(gestore, SIGCHLD)
void gestore(int signo)
{
    int stato;
    printf("In esecuzione gestore di SIGCHLD\n");
    wait(&stato);
    printf("Stato figlio: %d\n", stato >> 8);
}

int main(int argc, char **argv)
{

    int sock, open_conn_sock, udp_fd, nready, maxfdnum;
    char buffer[BUFFER_SIZE];
    fd_set readset;
    int len, nread, nwrite, port;
    const int on = 1;
    struct sockaddr_in clientaddr, servaddr;
    struct hostent *host;
    struct linea_struct bikes[N];

    if (argc != 1)
    {
        printf("Numero di parametri sbagliati...\n");
        exit(1);
    }

    port = 9000;
    if (port == 0 || port < 1024 || port > 65535)
    {
        printf("Fornisci una porta valida...\n");
        exit(1);
    }
    printf("In ascolto su %d", port);

    memset((char *)&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port = htons(port);

    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0)
    {
        perror("socket");
        exit(2);
    }
    printf("Socket TCP creata\n");

    udp_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (udp_fd < 0)
    {
        perror("socket");
        exit(2);
    }
    printf("Socket UDP creata\n");

    if (setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
    {
        perror("etsockopt");
        exit(2);
    }
    printf("Socket opt TCP settate\n");

    if (setsockopt(udp_fd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
    {
        perror("etsockopt");
        exit(2);
    }
    printf("Socket opt UDP settate\n");

    if (bind(sock, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0)
    {
        perror("bind");
        exit(2);
    }
    printf("Bind TCP successful\n");

    if (bind(udp_fd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0)
    {
        perror("bind");
        exit(2);
    }
    printf("Bind UDP successful\n");

    if (listen(sock, 5) < 0)
    {
        perror("listen");
        exit(2);
    }
    printf("Coda d'ascolto creata\n");

    signal(SIGCHLD, gestore);

    //Setup struttura dati
    for(int i = 0; i<N; i++){
        strcpy(bikes[i].id, "L");
        bikes[i].scadenza[0] = -1;
        bikes[i].scadenza[1] = -1;
        bikes[i].scadenza[2] = -1;
        strcpy(bikes[i].brand, "L");
        strcpy(bikes[i].folderName, "L");
    }

    //Init some random values
    strcpy(bikes[0].id, "AA123AA");
    bikes[0].scadenza[0] = 11;
    bikes[0].scadenza[1] = 9;
    bikes[0].scadenza[2] = 2022;
    strcpy(bikes[0].brand, "brand1");
    strcpy(bikes[0].folderName, "AA123AA_img/");
    strcpy(bikes[0].id, "BB987BB");
    bikes[2].scadenza[0] = 12;
    bikes[2].scadenza[1] = 11;
    bikes[2].scadenza[2] = 2023;
    strcpy(bikes[2].brand, "brand2");
    strcpy(bikes[2].folderName, "BB987BB_img/");

    while (1)
    {
        FD_SET(sock, &readset);
        FD_SET(udp_fd, &readset);
        maxfdnum = (udp_fd > sock)? udp_fd+1:sock+1;
        printf("In attesa di select...\n");
        printf("Con maxfd: %d\n", maxfdnum);
        if ((nready = select(maxfdnum, &readset, NULL, NULL, NULL)) < 0)
        {
            if (errno == EINTR)
            {
                continue;
            }
            else
            {
                perror("select");
                exit(3);
            }
        }
        if (FD_ISSET(udp_fd, &readset))
        {
            char serial[32] = "";
            char filename[1024] = "";
            char imagePath[1024+32+1];
            char *strToken;
            int pict_fd;
            int result;
            
            // Richiesta udp
            printf("Richiesta UDP arrivata\n");
            //Attendo richiesta come stringa: ID_SERIALE|filename/
            if ((nread = recvfrom(udp_fd, buffer, sizeof(buffer), 0, (struct sockaddr *)&clientaddr, &len)) < 0)
            {
                perror("recvfrom");
                continue;
            }

            strToken = strtok(buffer, "|");
            printf("PRimo strtok %s\n", strToken);
            for(int i = 0; i<7; i++){
                serial[i] = strToken[i];
            }
            serial[7] = '\0';
            strToken = strtok(NULL, "/");
            printf("secondo strtok %s\n", strToken);
            strcpy(filename, strToken);
            strcpy(imagePath, serial);
            strcat(imagePath, "_img/");
            strcat(imagePath, filename);
            printf("Richiesta immagine %s \n", imagePath);
            printf("Seriale %s\n", serial);
            printf("ImagePAth %s\n", imagePath);

            //Verifico che ci sia il file
            if((pict_fd = open(imagePath, O_RDONLY)) < 0){
                printf("File non trovato, ritorno -1...\n");
                result = -1;
            }else{
                close(pict_fd);
                if(remove(imagePath) != 0){
                    printf("Altro errore in fase di eliminazione ritorno -1...\n");
                    result = -1;
                }else{
                    return 0;
                }
            }

            if (sendto(udp_fd, &result, sizeof(int), 0, (struct sockaddr *)&clientaddr, len) < 0)
            {
                perror("sendto");
                continue;
            }
            printf("Risolta richiesta UDP\n");
        }
        if (FD_ISSET(sock, &readset))
        {
            // Richiesta TCP
            printf("Richiesta TCP arrivata\n");
            if ((open_conn_sock = accept(sock, (struct sockaddr *)&clientaddr, &len)) < 0)
            {
                if (errno == EINTR)
                {
                    continue;
                }
                else
                {
                    perror("accept");
                    continue;
                }
            }
            if (fork() == 0)
            {
                close(sock);
                while(1){
                    //Attendo ID seriale, poi numero immagini, poi per ogni  size immagine, nome immagine, zero binario e immagine
                    
                    char seriale[7];
                    int imageNum; 
                    int imageSize; 
                    char imageName[1024];
                    char imagePath[2048];
                    char readChar;
                    int imageFd;

                    if (read(open_conn_sock, seriale, sizeof(seriale)) <= 0)
                    {
                        perror("read");
                        break;
                        //Se becco un broken pipe esco dal loop e chiudo il figlio siccome vuol dire che il client ha chiuso la connessione
                    }
                    printf("Letto seriale %s\n", seriale);
                    
                    if (read(open_conn_sock, &imageNum, sizeof(int)) <= 0)
                    {
                        perror("read");
                        exit(4);
                    }
                    imageNum = ntohl(imageNum);
                    printf("Attendo %d immagini\n", imageNum);

                    for(int i = 0; i<imageNum; i++){
                        if (read(open_conn_sock, &imageSize, sizeof(int)) <= 0)
                            {
                            perror("read");
                            exit(4);
                        }
                        imageSize = ntohl(imageSize);
                        printf("Attendo immagine di %d bytes\n", imageSize);
                        strcpy(imageName, "");
                        /*do{
                            //TODO Sarebbe bene controllare errori in read
                            read(open_conn_sock, &readChar, 1);
                            if(readChar != 0){
                                imageName[strlen(imageName)] = readChar;
                                imageName[strlen(imageName)+1] = '\0';
                            }
                        }while(readChar != 0);*/
                        int cont = 0;
                        read(open_conn_sock, &readChar, 1);
                        while(readChar != 0){
                            imageName[cont] = readChar;
                            cont++;
                            read(open_conn_sock, &readChar, 1);
                        }
                        imageName[cont] = "\0";

                        strcpy(imagePath, "");
                        strcat(imagePath, seriale);
                        strcat(imagePath, "_img/");
                        strcat(imagePath, imageName);
                        printf("Mi preaparo a ricevere file: %s\n", imagePath);

                        if((imageFd = open(imagePath, O_WRONLY | O_CREAT)) < 0){
                            printf("Errore nella creazione della nuova immagine...\n");
                            exit(5);
                        }
                        nread = 0; 
                        while(imageSize > 0){
                            nread = read(open_conn_sock, buffer, sizeof(buffer));
                            write(imageFd, buffer, nread);
                            imageSize -= nread;
                        }
                        close(imageFd);
                        printf("Finito la scrittura dell'immagine\n");
                    } 

                    printf("Finita la scrittura di tutte le immagini..\n");
                }
                
                close(open_conn_sock);
                exit(0);
            }
            close(open_conn_sock);
        }
    }
}

