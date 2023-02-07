#include <stdio.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#define LINE_LENGTH 256

/***************************************/
typedef struct {
    int op1; int op2; char tipoOp;
} Request;
/**************************************/

int main(int argc, char **argv){
    struct hostent *host;
    struct sockaddr_in clientaddr, servaddr;
    int port, sd, num1, num2, len, ris;
    char okstr[LINE_LENGTH];
    char c; int ok;
    Request req;
    // Controllo argomenti
    if(argc!=3){ 
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(1);
    }
    // Inizializzazione indirizzo client e server e fine controllo argomenti

    // memset serve per riempire l'area di memoria puntanta con tutti byte uguali (in questo caso tutti 0)
    memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
    clientaddr.sin_family = AF_INET; // Famiglia di indirizzamento (Nel nostro caso sempre questa perchè lavoriamo in internet)
    // INADDR_ANY indica che accettiamo le richieste a tutti gli indirizzi IP validi per noi
    clientaddr.sin_addr.s_addr = INADDR_ANY; // Indirizzo IP del nodo locale, cioè del cliente
    clientaddr.sin_port = 0; // Numero di porta, messo uguale a 0 significa una porta qualsiasi, non ci interessa
    
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    host = gethostbyname (argv[1]); // Restituisce l'indirizzo internet del nome logico corrispondente (usa DNS)
    
    /*
     * Verifica correttezza porta e host: farla al meglio
     */
    num1=0;
    while( argv[2][num1]!= '\0' ){
        if( (argv[2][num1] < '0') || (argv[2][num1] > '9') ){
            printf("Secondo argomento non intero\n");
            exit(2);
        }
        num1++;
    }

    port = atoi(argv[2]);
    if (port < 1024 || port > 65535){
        printf("Port scorretta...");
        exit(2);
    }
    if (host == NULL){
        printf("Host not found ...");
        exit(2);
    }else{ // valori corretti
        servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr; // h_addr è il nome fisico primario dell'host
        servaddr.sin_port = htons(port);
    }
    /*
     * Primitiva per creale LOCALMENTE la socket -> Il legame con il nome globale del nodo corrente viene fatto con la bind()
     * socket(dominio, tipo, protocollo)
     * - dominio: Per noi sempre internet -> AF_INET
     * - tipo : SOCK_DGRAM (datagram), SOCK_STREAM
     * - protocollo : se messo a 0 viene utilizzato il protocollo di default del tipo di socket specificato
     */
     
    sd=socket(AF_INET, SOCK_DGRAM, 0); // Creazione socket -> Ricordarsi poi di chiuderla una volta finito il trasferimento di dati
    if(sd<0) {
        perror("apertura socket");
        exit(1);
    }
    /*
     * Primitiva per collegare la socket creata localmente a porta e nodo globali visibili
     * bind(sd, addr, addrlen)
     * - sd: socket descriptor che identifica la socket (livello locale)
     * - addr: struttura con indirizzo di porta del nodo (nome con il quale gli altri mi trovano)
     * - addrlen: la lunghezza di addr
     */
    if(bind(sd,(struct sockaddr *) &clientaddr, sizeof(clientaddr))<0){
        perror("bind socket ");
        exit(1);
    }

    printf("Inserisci il primo operando (int), EOF per terminare:");
    while ((ok = scanf("%i", &num1)) != EOF){ // Filtro
        if (ok != 1) {// errore di formato
            /* Problema nell’implementazione della scanf. Se l’input contiene PRIMA dell’intero
            * altri caratteri la testina di lettura si blocca sul primo carattere (non intero) letto.
            * Ad esempio: |ab1292\n|
            * ^ La testina si blocca qui
            * Bisogna quindi consumare tutto il buffer in modo da sbloccare la testina. */
            do{
                c=getchar();
                printf("%c ", c);
            }while (c!= '\n');

            printf("Inserisci il primo operando (int), EOF per terminare: ");
            continue;
        }
        req.op1=htonl(num1);
        fgets(okstr, LINE_LENGTH,stdin); // Consumo il resto della linea
        printf("Secondo operando (intero): ");
        while (scanf("%i", &num2) != 1){
            do{
                c=getchar();
                printf("%c ", c);
            }while (c!= '\n');
            printf("Secondo operando (intero): ");
        }
        // Non serve la htonl nelle prove d'esame
        req.op2=htonl(num2);
        fgets(okstr, LINE_LENGTH,stdin); // Consumo il resto della linea
        printf("Stringa letta: %s\n", okstr);
        do{
            printf("Operazione (+ = addizione, - = sottrazione, ... ");
            c = getchar();
        } while (c!='+' && c !='-' && c!='*' && c !='/');

        req.tipoOp=c;
        fgets(okstr, LINE_LENGTH,stdin); // Consumo il resto della linea
        printf("Operazione richiesta: %x %c %x \n", ntohl(req.op1),req.tipoOp, ntohl(req.op2));
        len=sizeof(servaddr); // Richiesta operazione
        /*
         * Primitiva per inviare datagrammi sulla socket datagram (per socket connesse si usa un'altra primitiva)
         * sendto(s, msg, len, flags, to, tolen)
         * - sd: Socket descriptor che identifica la socket
         * - msg: Stringa che contiene il messaggio da spedire
         * - len: Dimensione del messaggio
         * - flags: Eventuali flag di operazione (???)
         * - to: sockaddr del destinatario (in questo caso facciamo il cast sockaddr_in -> sockaddr)
         * - tolen: lunghezza dell'indirizzo del destinatario
         * 
         * Restituisce un intero che indica il numero di byte inviati
         */
        if(sendto(sd, &req, sizeof(Request), 0,(struct sockaddr *)&servaddr, len)<0){
            perror("sendto");
            continue;
        }
        /* ricezione del risultato */
        printf("Attesa del risultato...\n");
        /*
         * Primitva per ricevere datagrammi sulla socket datagram (per socket connesse si usa un'altra primitiva)
         * recvfrom(sd, buf, len, flags, from, fromlen)
         * - sd: Socket descriptor che identifica la socket
         * - buf: Area per contenere i dati in arrivo
         * - len: Dimensione del buf
         * - flags: Eventuali flag di operazione (???)
         * - from: sockaddr del mittente che mi sta inviando i dati
         * - fromlen: lunghezza dell'indirizzo del mittente
         * 
         * Restituisce un intero che indica il numero di byte ricevuti
         */
        if (recvfrom(sd, &ris, sizeof(ris), 0,(struct sockaddr *)&servaddr, &len)<0){
            perror("recvfrom");
            continue;
        }
        printf("Esito dell'operazione: %i\n", ntohl(ris));
        printf("Inserisci il primo operando (int), EOF per terminare:");
    }
    close(sd); // Libero le risorse: chiusura socket
    printf("\nClient: termino...\n");
    exit(0);
}