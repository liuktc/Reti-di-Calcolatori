/* Nome Cognome Matricola */

#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "server.h"

int main(int argc, char *argv[]) {
    char   *host; // nome host
    CLIENT *cl;   // gestore del trasporto
    char    ok[5];

    // Controllo degli argomenti
    if (argc != 2) {
        printf("usage: %s server_host\n", argv[0]);
        exit(1);
    }
    host = argv[1];

    // Creazione gestore del trasporto
    cl = clnt_create(host, ESAME, ESAMEVERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(host);
        exit(1);
    }

    // Interazione con l'utente
    printf("Inserire:\n1\tAggiungi stanza\n2\tElimina utente\n^D\tper terminare: ");

    while (gets(ok)) {

        /* 1 - Aggiungi stanza */
        if (strcmp(ok, "1") == 0) {
            Richiesta richiesta;
            printf("Inserisci il nome della stanza:\n");
            gets(richiesta.nomeStanza);
            printf("Inserisci lo stato della stanza(P/M/SP/SM):\n");
            gets(richiesta.tipo);

            int* risultato = aggiungi_stanza_1(&richiesta,cl);

            // Controllo del risultato
            if (risultato == NULL) {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }

            if((*risultato) == 0){
                printf("Aggiunta effettuata con successo!");
            }else{
                printf("Errore nell'aggiunta della stanza: %d",(*risultato));
            }
            
        }
        /* 2 - Elimina utente */
        else if (strcmp(ok, "2") == 0) {
            Utente utente;
            printf("Inserisci il nome dell'utente da eliminare:\n");
            gets(utente.nome);

            Risultato * risultato = elimina_utente_1(&(utente.nome),cl);

            // Controllo del risultato
            if (risultato == NULL) {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }

            if(risultato->res < 0){
                printf("Errore: l'utente %s non esiste in nessuna stanza",utente.nome);
            }else{
                printf("Utente eliminato dalle seguenti stanze:");
                for(int i=0;i<risultato->res;i++){
                    printf("%d) %s",i+1,risultato->stanze[i].nomeStanza);
                }
            }
        }
        /* 3 - Visualizza stato */
        else if(strcmp(ok, "3") == 0) {
            char ** risultato = visualizza_stato_1(NULL,cl);

            // Controllo del risultato
            if (risultato == NULL) {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }

            printf("%s",(*risultato));

        }else if(strcmp(ok,"4") == 0) {
            char nomeStanza[DIM_NOME];
            gets(nomeStanza);

            int* risultato = sospensione_stanza_1(*nomeStanza,cl);

            // Controllo del risultato
            if (risultato == NULL) {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }

            if((*risultato) == 0){
                printf("Stanza sospesa con successo");
            }else{
                printf("Errore nella sospensione della stanza: %d",(*risultato));
            }
        }else{
            printf("Operazione richiesta non disponibile!!\n");
        }

        printf("Inserire:\n1\tAggiungi stanza\n2\tElimina utente\n^D\tper terminare: ");
    }

    clnt_destroy(cl);
}