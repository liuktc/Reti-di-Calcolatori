import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class FileUtility {
	/**
	 * Nota: sorgente e destinazione devono essere correttamente aperti e chiusi
	 * da chi invoca questa funzione.
	 *  
	 */
	static protected void trasferisci_a_byte_file_binario(DataInputStream src,
			DataOutputStream dest, long dim) throws IOException {
	
		// ciclo di lettura da sorgente e scrittura su destinazione
	    int buffer, count=0;    
	    try {
	    	// esco dal ciclo all lettura di un valore negativo -> EOF
	    	// N.B.: la funzione consuma l'EOF
	    	while (((buffer=src.read()) >= 0)&&(count<dim-1)) {
	    		dest.write(buffer);
	    		count++;
	    		//System.out.println("Ottenuto il byte numero "+count+" : "+Character.toChars(buffer)[0]);
	    	}
	    	
	    	//dest.flush();
	    }
	    catch (EOFException e) {
	    	System.out.println("Problemi, i seguenti: ");
	    	e.printStackTrace();
	    }
	}
}
