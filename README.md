# TP5-DAR : Java RMI (Remote Method Invocation)

**Auteur :** Ahmed Salah
**Module :** DAR : Développement d'applications réparties
**Année :** 2026
**Sujet :** TP5 : Applications distribuées avec Java RMI et JNDI

---

## 1. Présentation

Ce TP met en pratique **Java RMI**, le mécanisme Java d'appel de méthodes à distance. Un **client** appelle des méthodes d'un objet qui s'exécute dans une autre JVM, le **serveur**, comme s'il s'agissait d'un objet local.

Le TP est composé de **deux applications client/serveur**, donc de **quatre projets** :

| Application | Serveur | Client | Annuaire |
|---|---|---|---|
| Conversion Euro → Dinar | `TP5_RMIConversion_Server` | `RMIConversion_Client` | `Naming` (rmiregistry, port 1099) |
| Gestion bancaire | `RMIBanque_Server` | `RMIBanque_Client` | JNDI (rmiregistry, port 1100) |

## 2. Objectifs pédagogiques

- Définir une **interface distante** (`extends Remote`).
- Implémenter un **objet distant** (`extends UnicastRemoteObject`).
- Démarrer un **annuaire RMI** (`LocateRegistry.createRegistry`) et y publier un objet.
- Retrouver un objet distant avec `Naming.lookup` ou avec **JNDI**.
- Transmettre un objet **sérialisable** (`Compte`) entre client et serveur.
- Gérer les erreurs distantes (`RemoteException`, exceptions métier).

## 3. Architecture

```
        Client (JVM 1)                         Serveur (JVM 2)
   ┌─────────────────────┐                ┌──────────────────────────┐
   │ appel de méthode    │                │  Objet distant (Impl)    │
   │        │            │   réseau TCP   │          ▲               │
   │      Stub  ─────────┼───────────────►│   Skeleton / UnicastRef  │
   └─────────▲───────────┘                └───────────▲──────────────┘
             │ lookup("nom")                          │ rebind("nom", obj)
             └───────────────►  rmiregistry  ◄────────┘
```

1. Le serveur crée le registre, crée l'objet distant et le publie sous un nom (`rebind`).
2. Le client demande l'objet par son nom (`lookup`) et reçoit un **stub** (mandataire).
3. Chaque appel sur le stub est transmis au serveur, exécuté sur l'objet réel, et le résultat est renvoyé.

## 4. Structure du dépôt

```
TP5-DAR/
├── README.md
├── TP5_RMIConversion_Server/
│   └── src/
│       ├── rmiService/IConversion.java      # interface distante
│       ├── rmiService/ConversionImpl.java   # implémentation (taux 1 Euro = 3.3 DT)
│       └── rmiServer/ConversionServer.java  # registre + publication sous le nom "BK"
├── RMIConversion_Client/                    # projet Maven
│   ├── pom.xml
│   └── src/main/java/
│       ├── rmiService/IConversion.java      # même interface que le serveur
│       └── rmiClient/ConversionClient.java  # convertit 500 Euro
├── RMIBanque_Server/
│   └── src/
│       ├── metier/Compte.java               # objet métier Serializable
│       ├── rmiService/IBanque.java          # interface distante
│       ├── rmiService/BanqueImpl.java       # implémentation (comptes en mémoire)
│       ├── rmiServer/BanqueServer.java      # registre + publication via JNDI
│       └── jndi.properties
└── RMIBanque_Client/
    └── src/
        ├── rmiClient/BanqueClient.java
        └── jndi.properties
```

## 5. Fonctionnalités

### 5.1 Service de conversion
- `double convertirMontant(double montantEuro)` renvoie le montant en dinars (taux fixe **1 Euro = 3.3 DT**, constante `TAUX` dans `ConversionImpl`).
- Le serveur affiche chaque requête reçue.

### 5.2 Service bancaire
| Méthode | Rôle |
|---|---|
| `creerCompte(Compte c)` | Crée un compte (erreur si le code existe déjà) |
| `getCompte(int code)` | Retourne le `Compte` (copie sérialisée côté client) |
| `getInfoCompte(int code)` | Retourne la description du compte sous forme de texte |
| `verser(int code, double montant)` | Ajoute un montant positif au solde |
| `retirer(int code, double montant)` | Retire un montant si le solde est suffisant |

Les méthodes du serveur sont `synchronized` : plusieurs clients peuvent se connecter simultanément sans corrompre les données.

## 6. Prérequis

- **JDK 1.8 ou supérieur** (testé avec JDK 1.8).
- **IntelliJ IDEA** (les projets contiennent leur configuration `.idea`), ou simplement `javac`/`java` en ligne de commande.
- Maven uniquement pour `RMIConversion_Client` (facultatif).

## 7. Exécution

> Ordre obligatoire : **serveur d'abord, client ensuite.**

### 7.1 Avec IntelliJ IDEA

**Conversion**
1. Ouvrir `TP5_RMIConversion_Server` et lancer `ConversionServer`.
2. Ouvrir `RMIConversion_Client` et lancer `ConversionClient`.

**Banque**
1. Ouvrir `RMIBanque_Server`, lancer `BanqueServer`.
2. Dans ce même projet : *Build → Build Artifacts → banqueClient → Build*. Cela génère `out/artifacts/banqueClient/banqueClient.jar`, qui contient `IBanque` et `Compte`.
3. Ouvrir `RMIBanque_Client` (la bibliothèque `banqueClient` pointe vers `../RMIBanque_Server/out/artifacts/banqueClient/banqueClient.jar`) et lancer `BanqueClient`.

> Les deux dossiers doivent donc rester **côte à côte**, avec leurs noms d'origine.

### 7.2 En ligne de commande (depuis la racine `TP5-DAR`)

```bash
# --- Conversion ---
mkdir -p out/cs out/cc
javac -d out/cs $(find TP5_RMIConversion_Server/src -name '*.java')
javac -d out/cc $(find RMIConversion_Client/src -name '*.java')
java -cp out/cs rmiServer.ConversionServer &        # terminal 1
java -cp out/cc rmiClient.ConversionClient          # terminal 2

# --- Banque ---
mkdir -p out/bs out/bc
javac -d out/bs $(find RMIBanque_Server/src -name '*.java')
javac -cp out/bs -d out/bc $(find RMIBanque_Client/src -name '*.java')
cp RMIBanque_Server/src/jndi.properties out/bs/
cp RMIBanque_Client/src/jndi.properties out/bc/
java -cp out/bs rmiServer.BanqueServer &            # terminal 1
java -cp out/bc:out/bs rmiClient.BanqueClient       # terminal 2 (sous Windows : out/bc;out/bs)
```

## 8. Résultats attendus

**Conversion**

Client :
```
500 Euro = 1650.0 DT
```
Serveur :
```
Serveur de conversion démarré, en attente de clients...
Requête reçue : conversion de 500.0 Euro
```

**Banque**

Client :
```
Connecté au service distant.
Compte [code=1, solde=1000.0, dateCreation=...]
Après versement de 500 : Compte [code=1, solde=1500.0, ...]
Après retrait de 200 : Compte [code=1, solde=1300.0, ...]
Erreur attendue : Aucun compte avec le code 99
```

Des captures d'écran de l'exécution se trouvent dans `RMIConversion_Client/screen/` et `RMIBanque_Client/screen/`.

## 9. Points techniques importants

- **Deux ports différents** : le service de conversion utilise `1099`, la banque `1100`, afin de pouvoir lancer les deux serveurs en même temps.
- **Interface partagée** : client et serveur doivent avoir la même interface distante (même package, même signature). Le client de conversion en possède une copie, le client de banque utilise le `.jar` généré par le serveur.
- **Sérialisation** : `Compte` implémente `Serializable`, sinon RMI lève une `NotSerializableException`.
- **JNDI** : `jndi.properties` (`RegistryContextFactory` + `rmi://localhost:1100`) évite d'écrire l'URL dans le code, il suffit de faire `ctx.lookup("BanqueService")`.
- **Erreurs** : les exceptions du serveur (compte inexistant, solde insuffisant) sont renvoyées au client, qui peut les traiter.

## 10. Problèmes courants

| Symptôme | Cause probable / solution |
|---|---|
| `ConnectException: Connection refused` | Le serveur n'est pas lancé, ou mauvais port |
| `ExportException: Port already in use` | Un ancien serveur tourne encore : l'arrêter |
| `NotBoundException` | Le nom ne correspond pas (`BK` / `BanqueService`) |
| `ClassNotFoundException: IBanque` | Le `.jar` `banqueClient` n'est pas généré ou pas ajouté au classpath du client |
| `NoInitialContextException` | `jndi.properties` absent du classpath |

## 11. Conclusion

Ce TP m'a permis de comprendre le fonctionnement de RMI : définition d'une interface distante, publication et recherche d'objets via `rmiregistry` ou JNDI, passage d'objets sérialisables, et gestion des appels concurrents.

---
© 2026 : Ahmed Salah
