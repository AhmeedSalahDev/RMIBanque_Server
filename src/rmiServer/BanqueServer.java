package rmiServer;

import rmiService.BanqueImpl;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.rmi.registry.LocateRegistry;

public class BanqueServer {

    public static void main(String[] args) {
        try {
            // Annuaire RMI sur le port 1100 (1099 est utilise par le service de conversion)
            LocateRegistry.createRegistry(1100);

            BanqueImpl banque = new BanqueImpl();

            // Publication via JNDI : la configuration est lue dans jndi.properties
            Context ctx = new InitialContext();
            ctx.rebind("BanqueService", banque);

            System.out.println("Serveur Banque demarre, objet publie sous le nom BanqueService.");
        } catch (Exception e) {
            System.err.println("Erreur Serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
