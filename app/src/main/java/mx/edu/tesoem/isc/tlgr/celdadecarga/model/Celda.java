package mx.edu.tesoem.isc.tlgr.celdadecarga.model;

public class Celda {

    String idc;
    String nomres;
    String produ;
    String peso;

    public Celda() {
    }

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc;
    }

    public String getNomres() {
        return nomres;
    }

    public void setNomres(String nomres) {
        this.nomres = nomres;
    }

    public String getProdu() {
        return produ;
    }

    public void setProdu(String produ) {
        this.produ = produ;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    @Override
    public String toString() {
        return produ + "\nPeso: " + peso ;
    }
}
