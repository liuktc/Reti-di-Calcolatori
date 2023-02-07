/* FiltroSemplice.java */

import java.io.*;

/**
 *
 * @author Fosco
 *
 * Un semplice filtro a linee che riporta in uscita le linee che contengono il
 * carattere 'a'
 *
 */
public class FiltroSemplice {

	public static void main(String[] args) {
		String line;
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter output = new BufferedWriter(new OutputStreamWriter(System.out));
		System.err
        	.println("\nScrivi quello che vuoi, ^D(Unix)/^Z(Win)+invio per uscire, porto in uscita le linee con almeno una 'a':");
		try {
			while ((line = input.readLine()) != null) {
				if (line.lastIndexOf('a') >= 0) output.write(line + "\n");
				output.flush();
			}
		}
		catch (IOException e) {
			System.out.println("Problemi, i seguenti: ");
			e.printStackTrace();
		}
	}
}