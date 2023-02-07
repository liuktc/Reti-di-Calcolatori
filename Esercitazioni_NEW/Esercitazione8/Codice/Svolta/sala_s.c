/* sala_s.c
 * 	+implementazione delle procedure remote: "prenota" e "visualizza".
 *	+inizializzazione struttura.
 */

#include "sala.h"
#include <fcntl.h>
#include <rpc/rpc.h>
#include <stdio.h>
#include <sys/stat.h>
#include <sys/types.h>

/* STATO SERVER */
static Sala sala;
static int  inizializzato = 0;

void inizializza() {
    int i, j;

    if (inizializzato == 1) {
        return;
    }

    // inizializzazione struttura dati
    for (i = 0; i < NUMFILE; i++) {
        for (j = 0; j < LUNGHFILA; j++) {
            sala.fila[i].posto[j] = 'L';
        }
    }

    sala.fila[1].posto[0] = 'D';
    sala.fila[2].posto[1] = 'P';
    sala.fila[5].posto[2] = 'P';

    for (j = 0; j < 3; j++) {
        sala.fila[8].posto[j] = 'B';
    }
    for (i = 1; i < 4; i++) {
        for (j = 4; j < 6; j++) {
            sala.fila[i].posto[j] = 'P';
        }
    }
    for (i = 5; i < 7; i++) {
        for (j = 4; j < 6; j++) {
            sala.fila[i].posto[j] = 'P';
        }
    }

    sala.fila[1].posto[6] = 'B';

    inizializzato = 1;
    printf("Terminata inizializzazione struttura dati!\n");
}

int *prenota_postazione_1_svc(Input *input, struct svc_req *rqstp) {
    static int result = -1;

    if (inizializzato == 0) {
        inizializza();
    }

    printf("Ricevuta richiesta di prenotazione per: %c, fila %i, colonna %i\n", input->tipo,
           input->fila, input->colonna);

    if (sala.fila[input->fila].posto[input->colonna] != 'L') {
        result = -2;
        return (&result);
    } else {
        sala.fila[input->fila].posto[input->colonna] = input->tipo;
        result                                       = 0;
        return (&result);
    }
} // prenota

Sala *visualizza_stato_1_svc(void *in, struct svc_req *rqstp) {
    if (inizializzato == 0) {
        inizializza();
    }
    return (&sala);
} // visualizza
