
public class Stanza {
    private final int NUM_UTENTI = 10;
    private String nome,stato;
    private String[] utenti;
    
    public Stanza(){
        this.nome = "L";
        this.stato = "L";
        this.utenti = new String[NUM_UTENTI];
        for(int i=0;i<NUM_UTENTI;i++){
            this.utenti[i] = "L";
        }
    }

    public String getNome(){
        return nome;
    }

    public synchronized void setNome(String nome){
        this.nome = nome; 
    }

    public String getStato(){
        return stato;
    }

    public synchronized void setStato(){

    }

    public boolean containsUser(String user){
        for(int i=0;i<NUM_UTENTI;i++){
            if(utenti[i].equals(user)){
                return true;
            }
        }
        return false;class
    }

    public synchronized boolean removeUser(String user){
        if(!containsUser(user))
            return false;
    
        for(int i=0;i<NUM_UTENTI;i++){
            if(utenti[i].equals(user)){
                utenti[i] = "L";
                return true;
            }
        }
        return false;
    }


}
