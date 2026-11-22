package metier;

import java.io.Serializable;
import java.util.Date;

/**
 * Compte bancaire. Serializable : l'objet est transmis par valeur
 * entre le client et le serveur RMI.
 */
public class Compte implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code;
    private double solde;
    private Date dateCreation;

    public Compte(int code, double solde) {
        this.code = code;
        this.solde = solde;
        this.dateCreation = new Date();
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public double getSolde() { return solde; }
    public void setSolde(double solde) { this.solde = solde; }

    public Date getDateCreation() { return dateCreation; }

    @Override
    public String toString() {
        return "Compte [code=" + code + ", solde=" + solde + ", dateCreation=" + dateCreation + "]";
    }
}
