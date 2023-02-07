// Cose con i file e i direttori in C
#include <dirent.h>
#include <stdio.h>
#include <string.h>

#define LEN 1024

void listaFileInCartella(char* direttorio){
    DIR *dir,*dir2;
    struct dirent *dd;
    int numeroFile = 0;
    int numeroCartelle = 0;
    char fullPath[LEN];

    
    if ((dir = opendir(direttorio)) == NULL) {
        return;
    }
    while ((dd = readdir(dir)) != NULL){
        if(strcmp(dd->d_name,".") != 0 && strcmp(dd->d_name,"..") != 0){
            snprintf(fullPath,sizeof(fullPath),"%s/%s",direttorio,dd->d_name);
            printf("%s\n",fullPath);
            if((dir2 = opendir(dd->d_name)) == NULL){
                numeroFile++;
            }else{
                numeroCartelle++;
            }
        }
    }
    char res[numeroFile][LEN];
    numeroFile = 0;
    dir = opendir(direttorio);
    while ((dd = readdir(dir)) != NULL){
        if(strcmp(dd->d_name,".") != 0 && strcmp(dd->d_name,"..") != 0){
            snprintf(fullPath,sizeof(fullPath),"%s/%s",direttorio,dd->d_name);
            //printf("%s\n",fullPath);
            if((dir2 = opendir(dd->d_name)) == NULL){
                strcpy(res[numeroFile],dd->d_name);
                numeroFile++;
            }
        }
    }

    printf("Numero di file: %d\nNumero di cartelle: %d\n",numeroFile,numeroCartelle);
}

void listaFileInSottocartelle(char *direttorio){
    DIR *dir,*dir2;
    struct dirent *dd,*dd2;
    int numeroFile = 0;
    int numeroCartelle = 0;
    char fullPath[LEN],fullPath2[LEN];

    
    if ((dir = opendir(direttorio)) == NULL) {
        return;
    }
    while ((dd = readdir(dir)) != NULL){
        if(strcmp(dd->d_name,".") != 0 && strcmp(dd->d_name,"..") != 0){
            sprintf(fullPath,"%s/%s",direttorio,dd->d_name);
            printf("1:%s\n",fullPath);
            if((dir2 = opendir(dd->d_name)) == NULL){
                numeroFile++;
            }else{
                numeroCartelle++;
                while((dd2 = readdir(dir2)) != NULL){
                    if(strcmp(dd2->d_name,".") != 0 && strcmp(dd2->d_name,"..") != 0){
                        snprintf(fullPath2,sizeof(fullPath2),"%s/%s",fullPath,dd2->d_name);
                        printf("2:%s\n",fullPath2);
                        // DA TESTARE
                        if(opendir(fullPath2) == NULL){
                            numeroFile++;
                        }else{
                            numeroCartelle++;
                        }
                    }
                }
            }
        }
    }

    printf("Numero di file: %d\nNumero di cartelle: %d\n",numeroFile,numeroCartelle);
}

int main(){
    listaFileInSottocartelle(".");
}