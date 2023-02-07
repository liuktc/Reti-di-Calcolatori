/* Nome Cognome Matricola */
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "server.h"


// variabili globali private (static)
static Stanza stanze[NUM_STANZE];
static int  inizializzato = 0;

/* Inizializza lo stato del server */
void inizializza() {
    int i;
    if (inizializzato == 1) {
        return;
    }
    for(int i=0;i<NUM_STANZE;i++){
        strcpy(stanze[i].nomeStanza,"L");
        strcpy(stanze[i].tipo,"L");
        for(int j=0;j<NUM_UTENTI;j++){
            strcpy(stanze[i].utenti[j].nome,"L");
        }
    }

    // Eventuali valori d'esempio

    inizializzato = 1;
    printf("Terminata inizializzazione struttura dati!\n");
}

int * aggiungi_stanza_1_svc(Richiesta *richiesta, struct svc_req *reqstp){
    if(!inizializzato){
        inizializza();
    }

    int res = 0;
    if(strcmp(richiesta->tipo,"P") != 0 &&
       strcmp(richiesta->tipo,"M") != 0 &&
       strcmp(richiesta->tipo,"SP") != 0 && 
       strcmp(richiesta->tipo,"SM") != 0){
        res=-1;
        return(&res);
       }
    for(int i=0;i<NUM_STANZE;i++){
        if(strcmp(stanze[i].nomeStanza,richiesta->nomeStanza) == 0){
            res = -1;
            return(&res);
        }
    }

    for(int i=0;i<NUM_STANZE;i++){
        if(strcmp(stanze[i].nomeStanza,"L") == 0){
            strcpy(stanze[i].nomeStanza,richiesta->nomeStanza);
            strcpy(stanze[i].tipo,richiesta->tipo);
            res = 0;
            return (&res);
        }
    }
}

Risultato * elimina_utente_1_svc(char **nomeUtente, struct svc_req *reqstp){
    if(!inizializzato){
        inizializza();
    }

    Risultato risultato;
    int cont = 0;
    for(int i=0;i<NUM_STANZE;i++){
        for(int j=0;j<NUM_UTENTI;j++){
            if(strcmp(stanze[i].utenti[j].nome,nomeUtente) == 0){
                strcpy(stanze[i].utenti[j].nome,"L"); // Eliminazione utente
                risultato.stanze[cont] = stanze[i];
                cont++;
            }
        }
    }
    risultato.res = cont;
    
    return  (&risultato);
}

char ** visualizza_stato_1_svc(void *n, struct svc_req *reqstp){
    if(!inizializzato){
        inizializza();
    }

    char risultato[NUM_STANZE*(NUM_UTENTI*DIM_NOME + DIM_NOME + 10)];

    strcpy(risultato,"");

    for(int i=0;i<NUM_STANZE;i++){
        strcat(risultato,stanze[i].nomeStanza);
        strcat(risultato,"\t");
        strcat(risultato,stanze[i].tipo);
        strcat(risultato,"\t");
        for(int j=0;j<NUM_STANZE;j++){
            strcat(risultato,stanze[i].utenti[j].nome);
            strcat(risultato,"\t");
        }
        strcat(risultato,"\n");
    }

    return *risultato;
}

int * sospensione_stanza_1_svc(char **nomeStanza, struct svc_req *reqstp){
    if(!inizializzato){
        inizializza();
    }
    int risultato = 0;

    for(int i=0;i<NUM_STANZE;i++){
        if(strcmp(stanze[i].nomeStanza,nomeStanza) == 0){
            if(strcmp(stanze[i].tipo,"SP") == 0 || strcmp(stanze[i].tipo,"SM") == 0){
                risultato = -1; 
            }else{
                if(strcmp(stanze[i].tipo,"P") == 0){
                    strcpy(stanze[i].tipo,"SP");
                    risultato = 0;
                }else if(strcmp(stanze[i].tipo,"M") == 0){
                    strcpy(stanze[i].tipo,"SM");
                    risultato = 0;
                }else{
                    risultato = -1;
                }
            }
        }
    }

    return (&risultato);
}