const DIM_NOME = 50;
const NUM_UTENTI = 10;
const NUM_STANZE = 100;

struct Utente{
    char nome[DIM_NOME];
};

struct Stanza{
    char nomeStanza[DIM_NOME];
    char tipo[3];
    Utente utenti[NUM_UTENTI];
};

struct Richiesta{
    char nomeStanza[DIM_NOME];
    char tipo[3];
};

struct Risultato{
    Stanza stanze[NUM_STANZE];
    int res;
};

program ESAME {
	version ESAMEVERS {         
		int aggiungi_stanza(Richiesta) = 1;
        Risultato elimina_utente(Utente) = 2;
	} = 1;
} = 0x20000013;