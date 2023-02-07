import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Consumatore {

	public static void main(String[] args) {
		BufferedReader in = null;
		String filterString;
		int found, c;
		char read;
		
		if (args.length < 1 || args.length>2){ //Invocation error
			System.out.println("Utilizzo: consumatore <prefix> [filename]");
			System.out.println("OR");
			System.out.println("Utilizzo: consumatore <prefix> < [filename]");
			System.exit(0);
		}
		else if (args.length == 1) {//only prefix
			System.out.println("Esecuzione con solo prefisso.");
			System.out.println("Prefisso = "+args[0] + " di lunghezza "+args[0].length());
			
			in = new BufferedReader(new InputStreamReader(System.in));		
		}
		else if (args.length == 2) {//prefix and filename
			System.out.println("Esecuzione con prefisso e file.");
			System.out.println("Prefisso = "+args[0] + " di lunghezza "+args[0].length()+
					"\nFile Path = "+args[1]);
			
			try {
				in = new BufferedReader(new FileReader(args[1]));
				System.out.println("File trovato. Filtraggio del file...");
			}catch(FileNotFoundException e){
				System.out.println("File non trovato");
				System.exit(1);
			}
		}

		filterString = args[0];

		try {
			// Filtro a carattere
			found = 0;
			while((c=in.read()) > 0){
				read = (char)c;
				for (int i=0; i<filterString.length(); i++) {
					if(read == filterString.charAt(i)) {
						found = 1;
					}
				}
				if(found == 0) {
					System.out.print(read);
				}
				found = 0;
			}
		} catch(IOException ex){
			System.out.println("Errore di input");
			System.exit(2);
		}
	}
		
}
