/* Nome Cognome Matricola */

#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "RPC_xFile.h"

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
    printf("Inserire:\n1\tVisualizza prenotazioni\n2\tAggiorna licenza\n^D\tper terminare: ");

    while (gets(ok)) {

        
        if (strcmp(ok, "1") == 0) {
            char tipo[7];
            printf("Inserisci il tipo di veicolo(auto o camper):\n");
            gets(tipo);
            if(strcmp(tipo, "auto")!=0 && strcmp(tipo, "camper")!=0){
                printf("Tipo di veicolo sbagliato");
                
                printf("Inserire:\n1\tVisualizza prenotazioni\n2\tAggiorna licenza\n^D\tper terminare: ");
                continue;
            }
            
            Output* risultato = visualizza_prenotazioni_1(&tipo, cl);

            // Controllo del risultato
            if (risultato == NULL) {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }else{
                int dim=risultato->dim;
                printf("Targa\t Patente\t Tipo\n");
                for(int i=0; i<dim; i++){
                    printf("%s\t%s\t%s\n",risultato->prenotazioni[i].targa,
                                          risultato->prenotazioni[i].patente,
                                          risultato->prenotazioni[i].tipo);
                }
            }
            
        }
        /* 2 - Modifica patente */
        else if (strcmp(ok, "2") == 0) {
            Input input;
            printf("Inserisci la targa:\n");
            gets(input.targa);
            printf("Inserisci num patente: \n");

            int * risultato = aggiorna_licenza(&(input),cl);

            // Controllo del risultato
            if (risultato == NULL) {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }

            if((*risultato) < 0){
                printf("Errore: non e' stato possibile modificare la licenza");
            }else{
                printf("Modificata la licenza con successo");
            }
        
        }else{
            printf("Operazione richiesta non disponibile!!\n");
        }

        printf("Inserire:\n1\tAggiungi stanza\n2\tElimina utente\n^D\tper terminare: ");
    }

    clnt_destroy(cl);
}