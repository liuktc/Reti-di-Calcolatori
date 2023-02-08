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

#define BUFFER_SIZE 254

// Chiama con ./Client

int main(int argc, char **argv)
{
    int sock, nread, port;
    char buffer[BUFFER_SIZE];
    char serialNum[7];
    char imageDir[2048];
    int imageFd;
    DIR *dir;
    struct dirent *dd;
    int imageNum; 
    int imageSize;
    struct hostent *host;
    struct sockaddr_in servaddr;

    if (argc != 1)
    {
        printf("Errore nel numero di argomenti!\n");
        exit(1);
    }

    port = 9000;
    if (port == 0 || port < 1024 || port > 65535)
    {
        printf("Errore, inserisci una porta giusta.");
        exit(1);
    }

    host = gethostbyname("localhost");
    if (host == NULL)
    {
        perror("gethostbyname");
        exit(2);
    }

    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    servaddr.sin_port = htons(port);

    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0)
    {
        perror("socket");
        exit(2);
    }

    if (connect(sock, (struct sockaddr *)&servaddr, sizeof(struct sockaddr)) < 0)
    {
        perror("connect");
        exit(2);
    }

    printf("Inserisci il nome del seriale di cui inviare il folder, EOF per uscire:\n");
    while (gets(serialNum) != 0)
    {
        strcpy(imageDir, serialNum);
        strcat(imageDir, "_img");
        printf("Folder da mandare: %s\n", imageDir);

        if(write(sock, serialNum, 7) != 7){
            perror("write");
            exit(2);
        }

        dir = opendir(imageDir);
        if(dir == NULL){
            printf("Dir non valida...");
            exit(3);
        }

        imageNum = 0;
        while((dd = readdir(dir)) != NULL){
            if(strcmp(dd->d_name, ".") != 0 && strcmp(dd->d_name, "..") != 0 && opendir(dd->d_name) == NULL){//dd->d_type != DT_DIR){
                imageNum++;
            }
        }
        printf("Mando al server il numero di immagini: %d\n", imageNum);
        imageNum = htonl(imageNum);
        if(write(sock, &imageNum, sizeof(int)) < 0){
            perror("write");
            exit(5);
        }
        
        closedir(dir);
        dir = opendir(imageDir);
        
        while((dd = readdir(dir)) != NULL){
            if(strcmp(dd->d_name, ".") != 0 && strcmp(dd->d_name, "..") != 0 && dd->d_type != DT_DIR){
                imageSize = 0;
                imageFd = open(dd->d_name, O_RDONLY);
                while((nread = read(imageFd, buffer, sizeof(buffer)) > 0)){
                    imageSize+=nread;
                }
                printf("Invio dimensione immagine: %d\n", imageSize);
                imageSize = htonl(imageSize);
                if(write(sock, &imageSize, sizeof(int)) <= 0){
                    perror("write");
                    exit(2);
                }
                printf("Invio nome immagine %s\n", dd->d_name);
                strcpy(buffer, dd->d_name);
                if(write(sock, buffer, strlen(buffer)) <= 0){
                    perror("write");
                    exit(2);
                }
                char binZero = 0;
                write(sock, &binZero, 1);
                printf("inizio a mandare immagine...\n");
                close(imageFd);
                imageFd = open(dd->d_name, O_RDONLY);
                while((nread = read(imageFd, buffer, sizeof(buffer)) > 0)){
                    write(sock, buffer, nread);
                }
                printf("Finito di scrivere immagine...\n");
                close(imageFd);
            }
        }
        closedir(dir);
        printf("Inserisci il nome del seriale di cui inviare il folder, EOF per uscire:\n");
    }
    close(sock);

    exit(0);
}
