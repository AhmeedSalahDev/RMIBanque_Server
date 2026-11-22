package rmiService;

import metier.Compte;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/** Implementation du service bancaire : les comptes sont gardes en memoire. */
public class BanqueImpl extends UnicastRemoteObject implements IBanque {

    private final Map<Integer, Compte> comptes = new HashMap<>();

    public BanqueImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized void creerCompte(Compte c) throws RemoteException {
        if (comptes.containsKey(c.getCode())) {
            throw new IllegalArgumentException("Le compte " + c.getCode() + " existe deja.");
        }
        comptes.put(c.getCode(), c);
        System.out.println("Compte cree : " + c);
    }

    @Override
    public synchronized Compte getCompte(int code) throws RemoteException {
        Compte c = comptes.get(code);
        if (c == null) {
            throw new IllegalArgumentException("Aucun compte avec le code " + code);
        }
        return c;
    }

    @Override
    public synchronized String getInfoCompte(int code) throws RemoteException {
        return getCompte(code).toString();
    }

    @Override
    public synchronized void verser(int code, double montant) throws RemoteException {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant doit etre positif.");
        }
        Compte c = getCompte(code);
        c.setSolde(c.getSolde() + montant);
    }

    @Override
    public synchronized void retirer(int code, double montant) throws RemoteException {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant doit etre positif.");
        }
        Compte c = getCompte(code);
        if (c.getSolde() < montant) {
            throw new IllegalArgumentException("Solde insuffisant (solde = " + c.getSolde() + ").");
        }
        c.setSolde(c.getSolde() - montant);
    }
}
