#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#define USAGE "Produttore filename"

int main(int argc, char** argv){
    int fd;
    char c;

    if(argc != 2){
        perror(" numero di argomenti sbagliato");
        exit(1);
    }

    fd = open(argv[1],O_WRONLY | O_CREAT | O_TRUNC, 00640);
    if (fd < 0) {
        perror("Impossibile creare/aprire il file");
        exit(2);
    }
    printf("Inserisci il testo e poi immetti EOF (Ctrl + D)\n");
    while(read(0,&c,sizeof(char)) > 0){
        write(fd,&c,sizeof(char));
    }
}