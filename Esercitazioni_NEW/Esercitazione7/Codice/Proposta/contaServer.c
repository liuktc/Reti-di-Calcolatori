/* contaServer.c */

#include "conta.h"
#include <dirent.h>
#include <fcntl.h>
#include <rpc/rpc.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

/********************************************************/
OutputFileScan *file_scan_1_svc(FileName *input, struct svc_req *rp) {
    static OutputFileScan result;
    int                   fd_file, nread;
    char                  cCorr;

    result.caratteri    = 0;
    result.parole       = 0;
    result.linee        = 0;
    result.codiceErrore = -1;

    printf("Richiesto file %s \n", input->name);

    fd_file = open(input->name, O_RDONLY);
    if (fd_file < 0) {
        printf("File inesistente\n");
        return (&result);
    } else {
        result.codiceErrore = 0;

        /* Conteggio caratteri */
        int  caratteri = 0;
        int  parole    = 0;
        int  linee     = 0;
        int  nread;
        char car;
        char prev = ' ';

        while (nread = read(fd_file, &car, 1) > 0) {
            caratteri += 1;
            if (car == '\n') {
                linee += 1;
            }
            if ((car == ' ' || car == '\n') && prev != ' ' && prev != '\n')
            { // Potrei usare un numero arbitrario di separatori.
                parole += 1;
            }
            prev = car;
        }

        if (nread < 0) { // Errore in lettura
            caratteri           = 0;
            parole              = 0;
            linee               = 0;
            result.codiceErrore = -1;
        } else {
            result.caratteri = caratteri;
            result.parole    = parole;
            result.linee     = linee;
            printf("Ho letto %d caratteri, %d parole e %d linee\n", result.caratteri, result.parole,
                   result.linee);
        }
        close(fd_file);
        return (&result);
    }
}

OutputDirScan *dir_scan_1_svc(Input *input, struct svc_req *rp) {
    DIR                 *dir;
    struct dirent       *dd;
    struct stat          stat;
    int                  i, fd_file, ret, cnt;
    char                 fullpath[256];
    static OutputDirScan result;

    printf("Richiesto direttorio %s\n", input->direttorio);
    if ((dir = opendir(input->direttorio)) == NULL) {
        result.nb_files = -1;
        return (&result);
    }

    // Max 8 files can be returned, so we insert also this control in the
    // while loop condition
    result.nb_files = 0;
    while ((dd = readdir(dir)) != NULL && result.nb_files < 8) {

        // Create a valid path from the current working directory
        snprintf(fullpath, sizeof(fullpath), "%s/%s", input->direttorio, dd->d_name);

        // Open file
        fd_file = open(fullpath, O_RDONLY);
        if (fd_file < 0) {
            printf("FILE: %s\n", fullpath);
            perror("Open file:");
            result.nb_files = -1;
            return (&result);
        }

        // Get file info
        ret = fstat(fd_file, &stat);

        // Check if it is a regular file and if the size is greater than the threshold
        if (S_ISREG(stat.st_mode) && stat.st_size >= input->soglia) {
            // printf("DEBUG: candidate file size %ld\n", stat.st_size);
            strcpy(result.files[result.nb_files].name, dd->d_name);
            result.nb_files++;
        }

        // Clean the path string
        memset(fullpath, 0, 256);
    }

    return (&result);
}
