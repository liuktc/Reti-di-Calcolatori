/* Nome Cognome Matricola */
#include <dirent.h>
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "RPC_xFile.h"
#define DIM_TOT 15
#define DIR_LEN 200

// variabili globali private (static)
static Prenotazione prenotazioni[DIM_TOT];
static int  inizializzato = 0;

/* Inizializza lo stato del server */
void inizializza() {
    int i;
    if (inizializzato == 1) {
        return;
    }
    for(i=0;i<DIM_TOT;i++){
        strcpy(prenotazioni[i].targa,"L");
        strcpy(prenotazioni[i].patente,"0");
        strcpy(prenotazioni[i].tipo,"L");
        strcpy(prenotazioni[i].folder,"L");
    }

    // Eventuali valori d'esempio

    inizializzato = 1;
    printf("Terminata inizializzazione struttura dati!\n");
}

Output * visualizza_prenotazioni_1_svc(char** tipo, struct svc_req *reqstp){
    if(!inizializzato){
        inizializza();
    }

    Output output;
    for(int i=0;i<DIM_TOT;i++){
        if(strcmp(prenotazioni[i].tipo,tipo) == 0){
            if(prenotazioni[i].targa[0]>"E" || (prenotazioni[i].targa[0]=="E" && prenotazioni[i].targa[1]>="D")){
                output.prenotazioni[output.dim] = prenotazioni[i];
                output.dim++;
            }
        }
    }
    return (&output);
}

int * aggiorna_licenza_1_svc(Input* input, struct svc_req *reqstp){
    if(!inizializzato){
        inizializza();
    }

    int risultato=-1;
    for(int i=0;i<DIM_TOT;i++){
        if(strcmp(prenotazioni[i].targa,input->targa)==0){
            strcpy(prenotazioni[i].patente,input->patente);
            risultato=0;
        }
    }
    
    return  (&risultato);
}
int* elimina_prenotazione(char** targa, struct svc_req *reqstp){
    if(!inizializzato){
        inizializza();
    }
    DIR *dir1;
    struct dirent *dd1;
    char newDir[DIR_LEN];
    int controllo, risultato=0;

    for(int i=0;i<DIM_TOT;i++){
        if(strcmp(prenotazioni[i].targa,targa)==0){
            if ((dir1 = opendir(prenotazioni[i].folder)) != NULL) {
                while ((dd1 = readdir(dir1)) != NULL) {
                // Ignoro le cartelle speciali . e ..
                    if (strcmp(dd1->d_name, ".") != 0 && strcmp(dd1->d_name, "..") != 0) {
                        snprintf(newDir,sizeof(newDir),"%s/%s",prenotazioni[i].folder,dd1->d_name);
                        if(opendir(newDir) == NULL){
                            // dd1 è un file
                            controllo=remove(newDir);
                            if(controllo!=0){
                                printf("errore nel rimuovere un file");
                                risultato=-1;
                                return &risultato;
                            }
                        }
                    }
                }
            }
                
        }
    }
    return &risultato;
}
