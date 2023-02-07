const NUM_GIUDICI = 12;
const MAX_NAME_SIZE = 128;
 
struct Giudice{
	char nomeGiudice[MAX_NAME_SIZE]; 
	int punteggioTot;
}; 

struct Output{
	Giudice classificaGiudici[NUM_GIUDICI]; 
}; 

struct Input{
	char nomeCandidato[MAX_NAME_SIZE];
	char tipoOp;
};
  
program OPERATION {
	version OPERATIONVERS {         
		Output CLASSIFICA_GIUDICI(void) = 1;        
        int ESPRIMI_VOTO(Input) = 2;
	} = 1;
} = 0x20000013;

