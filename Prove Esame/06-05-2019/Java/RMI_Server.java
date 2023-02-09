import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_ServerInterface {

    public RMI_Server() throws RemoteException {
        super();
    }
    int conta_consonanti(String nome){
        String consonanti="bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ";
        int count=0;
        for(int i=0; i<nome.length(); i++){
            if(consonanti.contains(nome.charAt(i)+"")){
                count++;
            }
        }
        return count;
    }
    
	String[] lista_file(String nome_dir) throws RemoteException{
        File dir=new File(nome_dir);
        File[] files, sottoFiles;
        int count=0;
        String[] res;
        if(dir.isDirectory()){
            files=dir.listFiles();
            for(int i=0; i<files.length; i++){
                if(files[i].isFile()){
                    if(conta_consonanti(files[i].getName())>=3){
                        count++;
                    }
                }else{
                    sottoFiles=files[i].listFiles();
                    for(int l=0; l<sottoFiles.length; l++){
                        if(sottoFiles[l].isFile()){
                            if(conta_consonanti(sottoFiles[l].getName())>=3){
                                count++;
                            }
                        }
                    }
                }
            }
            res=new String[count];
            count=0;
            for(int i=0; i<files.length; i++){
                if(files[i].isFile()){
                    if(conta_consonanti(files[i].getName())>=3){
                        res[count]=files[i].getName();
                        count++;
                    }
                }else{
                    sottoFiles=files[i].listFiles();
                    for(int l=0; l<sottoFiles.length; l++){
                        if(sottoFiles[l].isFile()){
                            if(conta_consonanti(sottoFiles[l].getName())>=3){
                                res[count]=sottoFiles[l].getName();
                                count++;
                            }
                        }
                    }
                }
            }
            if(res.length==0){
                throw new RemoteException("Nessun file da inviare");
                return null;
            }
            return res;

        }else{
            throw new RemoteException("Cartella non esiste");
            return null;
        }
    }

    int numerazione_righe(String nome_file) throws RemoteException{
        File file=new File(nome_file);
        String line;
        int count=1;
        int res=0;
        if(!file.exists() || !file.isFile()){
            return -1;
        }
        try{
            BufferedReader br=new BufferedReader(new FileReader(file));
            BufferedWriter bw= new BufferedWriter(new FileWriter(nome_file+"_temp"));
            while((line=br.readLine())!=null){
                if(count%2!=0){
                    bw.write(count+line);
                    count++;
                    res++;
                }else{
                    bw.write(line);
                    count++;
                }
            }
            br.close();
            bw.close();

            file.delete();
            
            File fileNew = new File(nome_file);
            File tempFile = new File(nome_file+"_temp");       
            
            tempFile.renameTo(fileNew);
            tempFile.delete();
            return res;
        }catch(Exception e){
            return -1;
        }
    }

    public static void main(String[] args) {
        final int PORT = 1099;
		String host = "localhost";
		String serviceName = "Server"; // lookup name...

		// Registrazione del servizio RMI
		String completeName = "//" + host + ":" + PORT + "/" + serviceName;
		try {
			RMI_Server serverRMI = new RMI_Server();
			Naming.rebind(completeName, serverRMI);
			System.out.println("Server RMI: Servizio \"" + serviceName + "\" registrato");
		} catch (Exception e) {
			System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
    }
	
}