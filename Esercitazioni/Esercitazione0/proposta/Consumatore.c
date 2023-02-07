#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

int main(int argc, char** argv){
    int fd,nread, trovato = 0;
    char *prefix;
    char c;

    if(argc == 2){
        // Caso con un solo argomento, ridirezione input
        fd = 0; // Leggo da stdin
        prefix = argv[1];
    }else if(argc == 3){
        // Caso con 2 argomenti, senza ridirezione
        fd = open(argv[2], O_RDONLY);
        if (fd < 0) {
            perror("P0: Impossibile creare/aprire il file");
            exit(2);
        }
        prefix = argv[1];
    }else{
        perror("Errore nel numero di argomenti!");
        exit(2);
    }

    while((nread = read(fd,&c,sizeof(char))) > 0){
        trovato = 0;
        for(int i=0;i<strlen(prefix);i++){
            if(c == prefix[i]){
                trovato = 1;
            }
        }

        if(trovato == 0){
            write(1,&c,sizeof(char));
        }
    }
}