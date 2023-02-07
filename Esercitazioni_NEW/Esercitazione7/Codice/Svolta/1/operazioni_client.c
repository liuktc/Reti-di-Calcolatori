/* operazioni_client.c
 *
 */

#include "operazioni.h"
#include <rpc/rpc.h>
#include <stdio.h>

int main(int argc, char *argv[]) {

    CLIENT  *cl;
    int     *ris;
    char    *server;
    Operandi op;

    if (argc != 5) {
        fprintf(stderr, "uso: %s host somma/moltiplicazione op1 op2\n", argv[0]);
        exit(1);
    }
    if (argv[2][0] != 'm' && argv[2][0] != 's') {
        fprintf(stderr, "uso: %s host somma/moltiplicazione op1 op2\n", argv[0]);
        fprintf(stderr, "il tipo di operazione deve iniziare per 's' o 'm'\n");
        exit(1);
    }

    server = argv[1];
    op.op1 = atoi(argv[3]);
    op.op2 = atoi(argv[4]);

    cl = clnt_create(server, OPERAZIONIPROG, OPERAZIONIVERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(server);
        exit(1);
    }

    if (argv[2][0] == 's') {
        ris = somma_1(&op, cl);
    }
    if (argv[2][0] == 'm') {
        ris = moltiplicazione_1(&op, cl);
    }
    if (ris == NULL) {
        clnt_perror(cl, server);
        exit(1);
    }

    printf("Operandi inviati a %s: %i e %i\n", server, op.op1, op.op2);
    printf("Risultato ricevuto da %s: %i\n", server, *ris);
    // Libero le risorse distruggendo il gestore di trasporto
    clnt_destroy(cl);
}
