/* echo.x 
 * 
 * 	+string = tipo predefinito di xdr. Nei file .c si deve usare l'equivalente C, cioé char*.
 * 	+Non si definiscono nuovi tipi, quindi NON viene generato il file con le routine di conversione echo_xdr.c.
 */

program ECHOPROG {
	version ECHOVERS {
		string ECHO(string) = 1;
	} = 1;
} = 0x20000013;


