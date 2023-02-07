#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <netdb.h>
char *ctime(); /* dichiarazione per formattazione dell'orario */
long int time (); /* tempo in secondi da inizio */
int ricevi ();
/* dichiarazione routine di ricezione di un messaggio*/
int s; /* socket descriptor del cliente */;
char * nomeprog;
struct hostent *hp; /* puntatore alle informazioni host remoto */
long timevar; /* contiene il risultato dalla time() */
struct sockaddr_in myaddr_in; /* socket address locale */
struct sockaddr_in peeraddr_in; /* socket address peer */

int ricevi (s, buf, n) int s; char * buf; int n;
{
    int i, j; /* ricezione di un messaggio di specificata lunghezza */
    i = recv(s,buf,n,0);
    if (i != n && i != 0) {
        if (i == -1) { 
            perror(nomeprog);
            fprintf(stderr,"%s: errore in lettura\n", nomeprog);
            exit(1);
        }
        while (i < n){
            j = recv (s, &buf[i], n-i, 0);
            if (j == -1) {
                perror(nomeprog);
                fprintf(stderr,"%s: errore in lettura\n", nomeprog);
                exit(1);
            }
            i += j;
            if (j == 0) break;
        }
    } /* si assume che tutti i byte arrivino ... se si verifica il fine file si esce */
    return i;
}


main(argc, argv) int argc; char *argv[];
{ 
    int addrlen, i;
    char buf[10]; /* messaggi di 10 bytes */
    if (argc != 3){
        fprintf(stderr, "Uso: %s <host remoto> <nric>\n", argv[0]);
        exit(1);
    }
    /* azzera le strutture degli indirizzi */
    memset ((char *)&myaddr_in, 0, sizeof(struct sockaddr_in));
    memset ((char *)&peeraddr_in, 0, sizeof(struct sockaddr_in));
    /* assegnazioni per il peer address da connettere */
    peeraddr_in.sin_family = AF_INET;
    /* richiede informazioni a proposito del nome dell'host */
    hp = gethostbyname (argv[1]);
    /* il risultato è già big endian e pronto per la TX */
    if (hp == NULL) {
        fprintf(stderr, "%s: %s non trovato in /etc/hosts\n", argv[0],
        argv[1]);
        exit(1);
    } /* trovato il nome IP fisico */
    peeraddr_in.sin_addr.s_addr =
    ((struct in_addr *)(hp->h_addr))->s_addr;
    /* non si usa la htonl dopo la gethostbyname: la si provi in
    diversi ambienti */
    struct servent *sp; /* puntatore alle informazioni del servizio */
    sp = getservbyname ("example", "tcp");
    if (sp == NULL){
        fprintf(stderr,"%s: non trovato in /etc/services\n",argv[0]);
        exit(1);
    }
    peeraddr_in.sin_port = htons (sp->s_port);/* invece */
    peeraddr_in.sin_port = htons(22375);
    s = socket (AF_INET,SOCK_STREAM,0);
    /* creazione della socket */
    if (s == -1) {
        perror(argv[0]); /* controllo errore */
        fprintf(stderr,"%s: non posso creare la socket\n", argv[0]); exit(1);
    }
    nomeprog = argv[0];
    /* per gestire condizioni d’errore in procedure */
    /* No bind: la porta del client assegnato dal sistema. Il server lo
    vede alla richiesta di connessione; il processo client lo ricava
    con getsocketname() */
    if(connect (s, &peeraddr_in, sizeof(struct sockaddr_in))== -1){
        perror(argv[0]); /* tentativo di connessione al server */
        fprintf(stderr,"%s: impossibile connettersi con server\n",argv[0]);
        exit(1);
    }
    /* altrimenti lo stream è stato ottenuto (!?) */
    addrlen = sizeof(struct sockaddr_in); /* dati connessione locale */
    if (getsockname (s, &myaddr_in, &addrlen) == -1){
        perror(argv[0]); fprintf(stderr, "%s: impossibile leggere il socket address\n", argv[0]); exit(1);
    }
    /* scrive un messaggio iniziale per l'utente */
    time(&timevar);
    printf("Connessione a %s sulla porta %u alle %s",
    argv[1], ntohs(myaddr_in.sin_port), ctime(&timevar));
    /* Il numero di porta espresso in byte senza convertire */
    sleep(5); /* attesa che simula un'elaborazione al client */
    for (i=1; i<= atoi(argv[2]); i++){/* invio di tutti i messaggi nel numero specificato dal secondo argomento */
        *buf = htonl(i); /* i messaggi sono solo gli interi successivi */
        if ( send (s, buf, 10, 0) != 10){
            fprintf(stderr, "%s: Connessione terminata per errore", argv[0]);
            fprintf(stderr, "sul messaggio n. %d\n", i); exit(1);
        }
    }
    if(shutdown (s, 1) == -1) {
        perror(argv[0]);
        fprintf(stderr, "%s: Impossibile eseguire lo shutdown della socket\n", argv[0]); exit(1);
    }
    /* Per ogni messaggio ricevuto, diamo un'indicazione locale */
    while (ricevi (s, buf, 10))
        printf("Ricevuta la risposta n. %d\n", ntohl( *buf));
    /* Messaggio per indicare il completamento del programma */
    time(&timevar);
    printf("Terminato alle %s", ctime(&timevar));
}