import java.io.*;
import java.time.Duration;
import java.time.LocalTime;

public class FileUtility {
    static protected void trasferisci_a_byte_file_binario (DataInputStream src, DataOutputStream dest,boolean diagnostic, int NUMERO_BYTE_PER_MESSAGGIO) throws IOException
    { 
        //int NUMERO_BYTE_PER_MESSAGGIO = 8;
        // ciclo di lettura da sorgente e scrittura su destinazione
        byte[] buffer = null;
        long cont = 0;
        LocalTime initialTime = LocalTime.now();
        try{ 
            // esco dal ciclo alla lettura di un valore negativo -> EOF
            while ( (buffer = src.readNBytes(NUMERO_BYTE_PER_MESSAGGIO)).length > 0){
                cont+= NUMERO_BYTE_PER_MESSAGGIO;
                dest.write(buffer);
                if(cont % 10000 == 0){
                    System.out.println("Trasferito " + cont + " bytes, current speed: " + (cont/
                    (Duration.between(initialTime, LocalTime.now()).getSeconds() + 1))/1000 + " kb/s");
                }
            }
            dest.flush();
        }
        catch (EOFException e){
            System.out.println("Problemi: ");
            e.printStackTrace();
        }
        //out.append(NUMERO_BYTE_PER_MESSAGGIO + "," + Duration.between(initialTime, LocalTime.now()).getSeconds());
    }
}
