/* echo_client.c
 *	+include echo.h
 */

#include "echo.h"
#include <rpc/rpc.h>
#include <stdio.h>
#define DIM 100

int main(int argc, char *argv[]) {

    CLIENT *cl;
    char  **echo_msg; // il risultato è un char*
    char   *server;
    char   *msg;
    char    ok[5];

    if (argc < 2) {
        fprintf(stderr, "uso: %s host\n", argv[0]);
        exit(1);
    }

    server = argv[1];

    cl = clnt_create(server, ECHOPROG, ECHOVERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(server);
        exit(1);
    }

    /* CORPO DEL CLIENT:
    /* ciclo di accettazione di richieste da utente ------- */
    msg = (char *)malloc(DIM + 1);

    printf("Dammi il messaggio (max 100 caratteri), EOF per terminare: ");

    while (gets(msg)) {
        echo_msg = echo_1(&msg, cl);
        if (echo_msg == NULL) {
            fprintf(stderr, "%s: %s fallisce la rpc\n", argv[0], server);
            clnt_perror(cl, server);
            exit(1);
        }

        /* In questo caso abbiamo la stringa nulla. Si noti che potrebbe essere
           utilizzata per notificare una condizione di errore al livello applicativo
           dal server al client */
        if (*echo_msg == NULL) {
            fprintf(stderr, "%s: %s restituisce una stringa nulla\n", argv[0], server);
        } else {
            printf("Messaggio consegnato a %s: %s\n", server, msg);
            printf("Messaggio ricevuto da %s: %s\n", server, *echo_msg);
        }
        printf("Dammi il messaggio (max 100 caratteri), EOF per terminare: ");

    } // while gets(msg)

    // Libero le risorse: memoria allocata con malloc e gestore di trasporto
    free(msg);
    clnt_destroy(cl);
    printf("Termino...\n");
    exit(0);
}
