//package proposta;
import java.io.*;

public class FileUtility {
    static protected void trasferisci_file_binario_n_byte(DataInputStream src,
			DataOutputStream dest,long byteDaLeggere, int NUMERO_BYTE_PER_MESSAGGIO) throws IOException {
	
		// ciclo di lettura da sorgente e scrittura su destinazione
	    byte[] buffer;   
		long byteLetti = 0; 
	    try {
	    	// esco dal ciclo all lettura di un valore negativo -> EOF
	    	// N.B.: la funzione consuma l'EOF
	    	/*while ((buffer=src.readNBytes(NUMERO_BYTE_PER_MESSAGGIO)).length >= 0) {
	    		dest.write(buffer);
	    	}*/
			while(byteLetti < byteDaLeggere){
				if(byteDaLeggere - byteLetti > NUMERO_BYTE_PER_MESSAGGIO){
					buffer=src.readNBytes(NUMERO_BYTE_PER_MESSAGGIO);
					dest.write(buffer);
					byteLetti+=buffer.length;
				}else{
					buffer=src.readNBytes((int)(byteDaLeggere - byteLetti));
					dest.write(buffer);
					byteLetti+=buffer.length;
				}
			}
	    	dest.flush();
			System.out.println("Finito di trasferire " + byteDaLeggere + " bytes");
	    }
	    catch (EOFException e) {
	    	System.out.println("Problemi, i seguenti: ");
	    	e.printStackTrace();
	    }
	}
}
