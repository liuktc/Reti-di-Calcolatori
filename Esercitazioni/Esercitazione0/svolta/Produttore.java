import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

// Produttore NON e' un filtro
public class Produttore {
	public static void main(String[] args) {		
		BufferedReader in = null;
		int res = 0;
		
		if (args.length != 1){
			System.out.println("Utilizzo: produttore <inputFilename>");
			System.exit(0);
		}
		
		System.out.println("Quante righe vuoi inserire?");
		in = new BufferedReader(new InputStreamReader(System.in));
			
		FileWriter fout;
		String inputl = null;
		try {
			fout = new FileWriter(args[0]);
			res = Integer.parseInt(in.readLine());
			for (int i =0; i<res; i++){
				System.out.println("Inserisci la nuova riga");
				inputl = in.readLine()+"\n";
				fout.write(inputl, 0, inputl.length());
			}		
			fout.close();
		} 
		catch (NumberFormatException nfe) { 
			nfe.printStackTrace(); 
			System.exit(1); // uscita con errore, intero positivo a livello di sistema Unix
		}
	    catch (IOException e) { 
			e.printStackTrace();
			System.exit(2); // uscita con errore, intero positivo a livello di sistema Unix
		}
	}
}

