/* conta.x
 *
 */

struct Input{
	char direttorio[256];
	int soglia;
};

struct OutputFileScan {
	int caratteri;
	int parole;
	int linee;
  	int codiceErrore;
};

struct FileName{
	char name[256];
};

struct OutputDirScan {
	int nb_files;
	FileName files[8];
};

program CONTAPROG {
	version CONTAVERS {
    	OutputFileScan FILE_SCAN(FileName) = 1;
		OutputDirScan DIR_SCAN(Input) = 2;
	} = 1;
} = 0x20000015;
