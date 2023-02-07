#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#define MAX_STRING_LENGTH 256

// produttore.c NON e' un filtro
int main(int argc, char *argv[]) {
    int   fd, readValues, bytes_to_write, written, righe, i;
    char *file_out;
    char  riga[MAX_STRING_LENGTH], buf[MAX_STRING_LENGTH];

    // controllo numero argomenti
    if (argc != 2) {
        perror(" numero di argomenti sbagliato");
        exit(1);
    }

    file_out = argv[1];
    printf("Quante righe vuoi inserire?\n");
    readValues = scanf("%d", &righe);
    if (readValues != 1) {
        printf("Errore: immettere un intero!");
        exit(1);
    }
    gets(buf); // consumare il fine linea

    fd = open(file_out, O_WRONLY | O_CREAT | O_TRUNC, 00640);
    if (fd < 0) {
        perror("P0: Impossibile creare/aprire il file");
        exit(2);
    }

    for (i = 0; i < righe; ++i) {
        printf("Inserisci la nuova riga\n");
        gets(riga);
        /* la gets legge tutta la riga, separatori inclusi, e trasforma il fine
           linea in fine stringa */
        riga[strlen(riga)]     = '\n'; // aggiungo il fine linea
        riga[strlen(riga) + 1] = '\0'; // aggiungo 0 binario per permettere una corretta strlen()
        written = write(fd, riga, strlen(riga)); // uso della primitiva
        if (written < 0) {
            perror("P0: errore nella scrittura sul file");
            exit(3);
        }
    }
    close(fd);
}
