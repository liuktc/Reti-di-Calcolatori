public class Prenotazione {
    
    private String targa;
    private String patente;
    private String tipo;
    private String folder;


    public Prenotazione(){
        this.targa = "L";
        this.patente = "0";
        this.tipo = "L";
        this.folder = "L";
    }
    //metodi get e set
    public String getTarga(){
        return this.targa;
    }
    public String getPatente(){
        return this.patente;
    }
    public String getTipo(){
        return this.tipo;
    }
    public String getFolder(){
        return this.folder;
    }
    public void setTarga(String targa){
        this.targa = targa;
    }
    public void setPatente(String patente){
        this.patente = patente;
    }
    public boolean setTipo(String tipo){
        if(!tipo.equals("auto") && 
           !tipo.equals("camper") &&
           !tipo.equals("L") ){
                return false;
           }
        this.tipo = tipo;
        return true;
    }
    public void setFolder(String folder){
        this.folder = folder;
    }
}
