package rmiService;

import metier.Compte;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Interface distante du service bancaire. */
public interface IBanque extends Remote {
    void creerCompte(Compte c) throws RemoteException;
    Compte getCompte(int code) throws RemoteException;
    String getInfoCompte(int code) throws RemoteException;
    void verser(int code, double montant) throws RemoteException;
    void retirer(int code, double montant) throws RemoteException;
}
