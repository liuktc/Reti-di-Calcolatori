/* echo_proc.c
 * 	+implementazione della procedura remota "echo".
 * 	+include echo.h.
 */

#include "echo.h"
#include <rpc/rpc.h>
#include <stdio.h>

char **echo_1_svc(char **msg, struct svc_req *rp) {
    /*
     * Il parametro di uscita e' statico ed e' da allocare.
     * Non serve allocare *msg, fatto dal supporto rpc.
     */
    static char *echo_msg;
    free(echo_msg);
    /*
     * ATTENZIONE, usare strlen e NON
     * sizeof(*msg) che restituisce
     * NON la dimensione della stringa puntata da *msg,
     * MA la dimensione di un puntatore, ovvero 4 byte.
     */
    echo_msg = (char *)malloc(strlen(*msg) + 1);

    printf("Messaggio ricevuto: %s\n", *msg);
    strcpy(echo_msg, *msg);
    printf("Messaggio da rispedire: %s\n", echo_msg);
    return (&echo_msg);
}
