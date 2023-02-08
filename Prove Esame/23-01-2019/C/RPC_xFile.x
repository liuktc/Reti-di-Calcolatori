const DIM_FOLDER = 50;

struct Prenotazione{
    char targa[8];
    char patente[6];
    char tipo[7];
    char folder[DIM_FOLDER];
};

struct Output{
    Prenotazione prenotazioni[6];
    int dim;
};

struct Input{
    char targa[8];
    char patente[6];
};

program ESAME {
	version ESAMEVERS {         
		Output visualizza_prenotazioni(string tipo) = 1;
        int aggiorna_licenza(Input) = 2;
	} = 1;
} = 0x20000013;