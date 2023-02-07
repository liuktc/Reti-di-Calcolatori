package Esercitazioni.Esercitazione0.proposta;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class Consumatore {
    public static void main(String[] args) {
        InputStream is = null;
        String prefix;
        int c;
        char carattere;
        if(args.length == 1){
            // Leggo da standard input
            is = System.in;
        }else if(args.length == 2){
            try {
                is = new FileInputStream(args[1]);
            } catch (FileNotFoundException e) {
                System.out.println("Errore nell'apertura del file " + args[1]);
                System.exit(0);
            }
        }else{
            System.out.println("Utilizzo: produttore <inputFilename>");
			System.exit(0);
        }
        prefix = args[0];
        try{
            while((c = is.read()) > 0){
                //carattere = (char) c;
                if(prefix.indexOf(c) < 0){
                    System.out.print(c);
                }   
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
