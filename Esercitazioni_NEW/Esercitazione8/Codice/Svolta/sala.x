/* 
 * sala.x
 *	+ definizione Input e struttura della sala.
 * 	+ definizione metodi e tipi richiesti/restituiti
 */

const LUNGHFILA=7;
const NUMFILE=10;

struct Input{
	char tipo;
	int fila;
	int colonna;
};

struct Fila{
	char posto[LUNGHFILA];
};

struct Sala {
	Fila fila[NUMFILE];
};


program SALA {
	version SALAVERS{
		int  PRENOTA_POSTAZIONE(Input) = 1;
		Sala VISUALIZZA_STATO(void) = 2;
	} = 1;
} = 0x20000013;
