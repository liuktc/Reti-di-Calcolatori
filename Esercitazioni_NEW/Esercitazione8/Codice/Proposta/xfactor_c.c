/*
 * xfactor_c.c
 */

#include "xfactor.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[]) {
    char   *host; // nome host
    CLIENT *cl;   // gestore del trasporto

    int    *ris;
    char    c;
    Output *classificaGiudici;
    char    ok[2], nl[2];
    Input   input;

    // Controllo degli argomenti
    if (argc != 2) {
        printf("usage: %s server_host\n", argv[0]);
        exit(1);
    }
    host = argv[1];

    // Creazione gestore del trasporto
    cl = clnt_create(host, OPERATION, OPERATIONVERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(host);
        exit(1);
    }

    // Interazione con l'utente
    printf("Inserire:\n1\tClassifica Giudici\n2\tEsprimi voto\n^D\tper terminare: ");

    while (scanf("%s", ok) == 1) {
	// Consuma fine linea
	gets(&nl);

        /********* 1 - Classifica Giudici *********/
        if (strcmp(ok, "1") == 0) {

            // Invocazione remota
            classificaGiudici = classifica_giudici_1(NULL, cl);
            // Controllo del risultato
            if (classificaGiudici == NULL) {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }

            printf("Classifica ordinata giudici:\n");

            for (int i = 0; i < NUM_GIUDICI; i++) {
                if (classificaGiudici->classificaGiudici[i].punteggioTot > 0) {
                    printf("%s con %d voti\n", classificaGiudici->classificaGiudici[i].nomeGiudice,
                           classificaGiudici->classificaGiudici[i].punteggioTot);
                }
            }

        }
        /********* 2 - Inserisci Voto *********/
        else if (strcmp(ok, "2") == 0)
        {
            printf("\nInserisci il nome del candidato: ");
            gets(input.nomeCandidato);

            printf("Inserisci tipo di operazione (A (addiziona) oppure (S) sottrai ): ");
            scanf("%c", &(input.tipoOp));
	    gets(&nl);

            // Invocazione remota
            ris = esprimi_voto_1(&input, cl);

            // Controllo del risultato
            if (ris == NULL) {
                // Errore di RPC
                clnt_perror(cl, host);
                exit(1);
            }

            if (*ris < 0) {
                printf("Problemi nell'attribuzione del voto, nome non trovato\n");
            } else if (*ris == 0) {
                printf("Votazione effettuata con successo\n");
            }
        } else {
            printf("Operazione richiesta non disponibile!!\n");
        }

        printf("Inserire:\n1\tGiudice in testa\n2\tEsprimi voto\n^D\tper terminare: ");
    } // while

    // Libero le risorse, distruggendo il gestore di trasporto
    clnt_destroy(cl);
    exit(0);
} // main
