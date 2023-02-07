/* sala_c.c
 *
 */

#include "sala.h"
#include <rpc/rpc.h>
#include <stdio.h>

#define LUNGHFILA 7
#define NUMFILE   10

int main(int argc, char *argv[]) {
    char   *host;
    CLIENT *cl;
    int    *ris, *start_ok;
    void   *in;
    Sala   *sala;
    Input   input;
    char    str[5];
    int     i, j, fila, col;
    char    c, ok[256];

    if (argc != 2) {
        printf("usage: %s server_host\n", argv[0]);
        exit(1);
    }
    host = argv[1];

    cl = clnt_create(host, SALA, SALAVERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(host);
        exit(1);
    }

    printf("Inserire:\nV) per vedere la sala\tP) per prenotare le sedie\t^D per terminare: ");
    while (gets(ok)) {
        if (strcmp(ok, "P") == 0) {
            printf("Tipologia di prenotazione (P, D o B): \n");
            gets(ok);

            // Leggo il tipo
            while ((strcmp(ok, "P") != 0) && (strcmp(ok, "D") != 0) && (strcmp(ok, "B") != 0)) {
                printf("Lettera sbagliata! Inserisci P, D o B: \n");
                gets(ok);
            }
            input.tipo = ok[0];

            // Leggo la fila
            fila = -1;
            while (fila < 0 || fila > (NUMFILE - 1)) {
                printf("Inserisci la fila (da 0 a %i): \n", (NUMFILE - 1));
                while (scanf("%d", &fila) != 1) {
                    /* Ricorda problema scanf...*/
                    do {
                        c = getchar();
                        printf("%c ", c);
                    } while (c != '\n');
                    printf("Fila: ");
                }
            }
            // Consumo fine linea
            gets(ok);
            input.fila = fila;

            // Leggo la colonna
            col = -1;
            while (col < 0 || col > (LUNGHFILA - 1)) {
                printf("Inserisci la colonna (da 0 a %i): \n", (LUNGHFILA - 1));
                while (scanf("%i", &col) != 1) {
                    /* Ricorda problema scanf...*/
                    do {
                        c = getchar();
                        printf("%c ", c);
                    } while (c != '\n');
                    printf("Colonna: ");
                }
            }
            // Consumo fine linea
            gets(ok);
            input.colonna = col;

            // Invocazione remota
            ris = prenota_postazione_1(&input, cl);
            if (ris == NULL) {
                clnt_perror(cl, host);
                exit(1);
            }
            if (*ris < 0) {
                if (*ris == -2) {
                    printf("Postazione Occupata\n");
                } else {
                    printf("Problemi nell'esecuzione della prenotazione\n");
                }
            } else {
                printf("Prenotazione effettuata con successo\n");
            }
        } // if P
        else if (strcmp(ok, "V") == 0)
        {
            sala = visualizza_stato_1(in, cl);
            if (sala == NULL) {
                clnt_perror(cl, host);
                exit(1);
            }
            printf("Stato di occupazione della sala:\n");
            for (i = 0; i < NUMFILE; i++) {
                for (j = 0; j < LUNGHFILA; j++) {
                    printf("%c\t", sala->fila[i].posto[j]);
                }
                printf("\n");
            }
        } // if V
        else
        {
            printf("Argomento di ingresso errato!!\n");
        }
        printf("Inserire:\nV) per vedere la sala\tP) per prenotare le sedie\t^D per terminare: ");
    } // while

    // Libero le risorse, distruggendo il gestore di trasporto
    clnt_destroy(cl);
    exit(0);
}
