import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMI_Server extends UnicastRemoteObject implements RMI_InterfaceFile {

    public RMI_Server() throws RemoteException {
        super();
    }
    public int elimina_occorrenze(String nomeFile) throws RemoteException {
		int c;
		char car;
		int count=0;
		try{
			FileReader fr=new FileReader(nomeFile);
			FileWriter fw=new FileWriter(nomeFile+"_temp");

			while((c=fr.read())>0){
				car=(char)c;
				if(!Character.isLowerCase(car)){
					fw.write(car);
				}
				else{
					count++;
				}
			}
			fr.close();
			fw.close();

			File originale=new File(nomeFile);
			originale.delete();
			File file=new File(nomeFile);
			File temp=new File(nomeFile+"_temp");
			temp.renameTo(file);
			temp.delete();

			return count;

		}catch(IOException e){
			e.printStackTrace();
			return -1;
		}
    }

	public String[] lista_filetesto(String dir) throws RemoteException {
		String[] res;
		int count=0;
		File folder=new File(dir);
		File[] files;
		if(folder.exists() && folder.isDirectory()){
			files=folder.listFiles();
			for(int i=0; i<files.length; i++){
				if(files[i].isFile() && files[i].getName().endsWith(".txt")){
					count++;
				}
			}
			if(count==0){
				res=new String[0];
				return res;
			}else if(count<=6){
				res=new String[count];
			}else{
				res=new String[6];
			}
			count=0;
			for(int i=0; i<files.length; i++){
				if(files[i].isFile() && files[i].getName().endsWith(".txt")){
					if(count<6){
						res[count]=files[i].getName();
						count++;
					}
				}
			}
		}else{
			return null;
		}
		return res;
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