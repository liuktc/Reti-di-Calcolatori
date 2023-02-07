
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

#define DIM_BUFF         100
#define LENGTH_FILE_NAME 20
#define LENGTH_PAROLA    20
#define max(a, b)        ((a) > (b) ? (a) : (b))
#define TEMP_FILE_NAME "tempFile"


int elimina_parola(char *nome_file, char* parola_da_eliminare) {
    char c;
    char parola[LENGTH_PAROLA];
    int fd,cont=0,cont_parola = 0,fd_temp;
    printf("Leggo il file\n");
    if((fd = open(nome_file,O_RDONLY)) < 0){
        perror("open(nome_file)");
        return -1;
    }
    printf("Creo il file temp\n");
    if((fd_temp = open(TEMP_FILE_NAME,O_WRONLY | O_CREAT,0777)) < 0){
        perror("open(temp_file)");
        return -1;
    }

    printf("Inizio a leggere il file\n");
    while(read(fd,&c,1) > 0){
        //write(1,&c,1);
        if(c == ' ' || c == '\n'){
            //parola[cont_parola] = c;
            parola[cont_parola] = '\0';
            if(strcmp(parola,parola_da_eliminare) == 0){
                // Ho trovato la parola da eliminare, non la stampo
                cont++;
            }else{
                printf("Stampo su file: ");
                // La posso stampare nel file temporaneo
                parola[cont_parola] = c;
                parola[cont_parola+1] = '\0';
                write(fd_temp,parola,strlen(parola));
                //write(1,parola,strlen(parola));
                printf("'%s'",parola);
                printf("\n");
            }
            cont_parola = 0;
        }else{
            parola[cont_parola] = c;
            cont_parola++;
        }
    }
    //parola[cont_parola] = c;
    parola[cont_parola] = '\0';
    if(strcmp(parola,parola_da_eliminare) == 0){
        // Ho trovato la parola da eliminare, non la stampo
        cont++;
    }else{
        // La posso stampare nel file temporaneo
        write(fd_temp,parola,strlen(parola));
        printf("'%s'",parola);
        //write(1,parola,strlen(parola));
    }

    // Dopo questo ciclo il file temp conterrà il nuovo file
    // Rimuovo l'originale e rinomino il temp
    if(remove(nome_file) != 0){
        perror("delete(nome_file)");
    }
    if(rename(TEMP_FILE_NAME,nome_file) != 0){
        perror("rename(tempFile,nome_file)");
    }
    close(fd_temp);
    return cont;
}

int main(int argc, char ** argv){
    elimina_parola("prova.txt",argv[1]);
}