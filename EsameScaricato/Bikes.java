//S0000971128, Dominici, Leonardo
package RMI;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Date;

public class Bikes {

	public static final int N = 4;
	
	public String seriali[];
	public int giorni[];
	public int mesi[];
	public int anni[];
	public String brand[];
	public String folder[];
	
	public Bikes() {
		super();
		seriali = new String[N];
		giorni = new int[N];
		mesi = new int[N];
		anni = new int[N];
		brand = new String[N];
		folder = new String[N];
		
		
		//Init struttura dati
		for(int i = 0; i<N; i++) {
			seriali[i] = "L";
			giorni[i] = -1;
			mesi[i] = -1;
			anni[i] = -1;
			brand[i] = "L";
			folder[i] = "L";
		}
		seriali[0] = "AA123AA";
		giorni[0] = 1;
		mesi[0] = 1;
		anni[0] = 2023;
		brand[0] = "brand1";
		folder[0] = seriali[0]+"_img/";
		seriali[1] = "BB987BB";
		giorni[1] = 12;
		mesi[1] = 5;
		anni[1] = 2022;
		brand[1] = "brand3";
		folder[1] = seriali[1]+"_img/";
	}
	
	public synchronized int elimina_prenot(int giorno, int mese, int anno) {
		//Date limitDate = new Date(anno, mese, giorno);
		//Date currentDate;
		
		File imageDir; 
		File files[];
		int deletedValues = 0;
		try {
			for(int i = 0; i<N; i++) {
				if(!seriali[i].equals("L")) {
					//currentDate = new Date(anni[i], mesi[i], giorni[i]);
					if(anno < anni[i] || (anno == anni[i] && mese<mesi[i]) || (anno == anni[i] && mese == mesi[i] && giorno < giorni[i])) {
						System.out.println("Cancello tutto di "+seriali[i]);
						imageDir = new File(folder[i]);
						files = imageDir.listFiles();
						for(int j = 0; j<files.length; j++) {
							if(files[j].isFile()) {
								files[j].delete();
								System.out.println("Eliminato un file dalla sua dir...");
							}
						}
						//Ora aggiorno la struttura dati
						seriali[i] = "L";
						giorni[i] = -1;
						mesi[i] = -1;
						anni[i] = -1;
						brand[i] = "L";
						folder[i] = "L";
						deletedValues++;
					}
				}
			}
		}catch(Exception e) {
			System.out.println("Errori incontrati...");
			e.printStackTrace();
			return -1;
		}
		System.out.println("Ho cancellato numero di entry: "+deletedValues);
		return deletedValues;
	}
	
	public synchronized Object[][] visual_prenot(int giorno, int mese, int anno){
		//Date limitDate = new Date(anno, mese, giorno);
		//Date currentDate;
		Object toRet[][] = new Object[6][5];
		int recordedEntries = 0;
		
		for(int i = 0; i<6; i++) {
			for(int j = 0; j<5; j++) {
				toRet[i][j] = null;
			}
		}
		
		try {
			for(int i = 0; i<N; i++) {
				if(!seriali[i].equals("L")) {
					//currentDate = new Date(anni[i], mesi[i], giorni[i]);
					if((anni[i] == anno && mesi[i] == mese && giorni[i] == giorno) ||
							(anno > anni[i] || (anno == anni[i] && mese>mesi[i]) || (anno == anni[i] && mese == mesi[i] && giorno > giorni[i]))) {
						toRet[recordedEntries][0] = seriali[i];
						toRet[recordedEntries][1] = giorni[i];
						toRet[recordedEntries][2] = mesi[i];
						toRet[recordedEntries][3] = anni[i];
						toRet[recordedEntries][4] = brand[i];
						recordedEntries++;
					}
				}
			}
		}catch(Exception e) {
			System.out.println("Errori incontrati...");
			e.printStackTrace();
			return null;
		}
		
		return toRet;
	}
	
}
