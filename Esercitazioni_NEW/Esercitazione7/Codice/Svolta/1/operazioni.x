/* 
 * operazioni.x
 *	Questo file definisce la struttura Operandi e i relativi metodi remoti.
 * 	Poi si dovra' procedere come segue:
 *  rpcgen operazioni.x --> genera i seguenti file:
 *		1) operazioni.h --> libreria da includere in operazioni_proc.c
 *					e operazioni_client.c.
 *		2) operazioni_xdr.c --> routine di conversione.
 * 		3) operazioni_clnt.c; operazioni_svc.c --> stub C e S.
 */

struct Operandi{
	int op1;
	int op2;
};


program OPERAZIONIPROG {
	version OPERAZIONIVERS {
		int SOMMA(Operandi) = 1;
		int MOLTIPLICAZIONE(Operandi) = 2;
	} = 1;
} = 0x20000013;


