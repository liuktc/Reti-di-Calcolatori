#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#define MAX_STRING_LENGTH 256

int main(int argc, char *argv[]) {
    int   fd, written;
    char *file_out;
    char  riga[MAX_STRING_LENGTH];

    file_out = argv[1];

    fd = open(file_out, O_WRONLY | O_CREAT | O_TRUNC, 00640);
    if (fd < 0) {
        perror("P0: Impossibile creare/aprire il file");
        return EXIT_FAILURE;
    }

    printf("Inserisci le nuove righe, o EOF [CTRL^D] per terminare\n");

    while (gets(riga)) {
        riga[strlen(riga)]     = '\n'; // aggiungo il fine linea
        riga[strlen(riga) + 1] = '\0'; // aggiungo 0 binario per permettere una corretta strlen()
        written                = write(fd, riga, strlen(riga));
        if (written < 0) {
            perror("P0: errore nella scrittura sul file");
            return EXIT_FAILURE;
        }
    }
    close(fd);
    return EXIT_SUCCESS;
}