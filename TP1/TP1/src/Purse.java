package purse;

import java.util.Scanner;
import java.util.Arrays;

public class Purse {
    private int[] userPIN;
    private int[] adminPIN;

    private final int MAX_USER_TRIES;
    private final int MAX_ADMIN_TRIES;
    private final int MAX_TRANS;
    private final int MAX_BALANCE;
    private final int MAX_CREDIT_AMOUNT;
    private final int MAX_DEBIT_AMOUNT;

    private int userTriesLeft;
    private int adminTriesLeft;
    private int balance;
    private int transLeft;
    private boolean userAuthenticate;
    private boolean adminAuthenticate;
    private LCS lifeCycleState;

    private final Scanner scanner = new Scanner(System.in);

    public Purse(int MAX_USER_TRIES,
                 int MAX_ADMIN_TRIES,
                 int MAX_TRANS,
                 int MAX_BALANCE,
                 int MAX_CREDIT_AMOUNT,
                 int MAX_DEBIT_AMOUNT,
                 int[] userPIN,
                 int[] adminPIN) {

        this.MAX_USER_TRIES = MAX_USER_TRIES;
        this.MAX_ADMIN_TRIES = MAX_ADMIN_TRIES;
        this.MAX_TRANS = MAX_TRANS;
        this.MAX_BALANCE = MAX_BALANCE;
        this.MAX_CREDIT_AMOUNT = MAX_CREDIT_AMOUNT;
        this.MAX_DEBIT_AMOUNT = MAX_DEBIT_AMOUNT;

        this.userPIN = userPIN.clone();
        this.adminPIN = adminPIN.clone();

        this.userTriesLeft = MAX_USER_TRIES;
        this.adminTriesLeft = MAX_ADMIN_TRIES;
        this.balance = 0;
        this.transLeft = MAX_TRANS;
        this.userAuthenticate = false;
        this.adminAuthenticate = false;
        this.lifeCycleState = LCS.USE;
    }

    public Purse(int[] userPIN, int[] adminPIN) {
        this(3, 4, 500, 100, 50, 30, userPIN, adminPIN);
    }

    public boolean verifyPINUser(int[] PINCode) {
        if (lifeCycleState == LCS.DEAD || lifeCycleState == LCS.BLOCKED) {
            return false;
        }
        return Arrays.equals(userPIN, PINCode);
    }

    public boolean verifyPINAdmin(int[] PINCode) {
        if (lifeCycleState == LCS.DEAD) {
            return false;
        }
        return Arrays.equals(adminPIN, PINCode);
    }

    private int[] readPINFromConsole(String label) {
        System.out.print("Veuillez saisir le PIN " + label + " (suite de chiffres, sans espace) : ");
        String line = scanner.nextLine().trim();

        if (line.isEmpty()) {
            throw new IllegalArgumentException("PIN vide interdit");
        }

        int[] pin = new int[line.length()];
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (!Character.isDigit(c)) {
                throw new IllegalArgumentException("Le PIN doit contenir uniquement des chiffres.");
            }
            pin[i] = c - '0';
        }
        return pin;
    }

    private boolean getIdentificationUser() {
        if (lifeCycleState != LCS.USE) {
            System.out.println("Impossible d'identifier l'utilisateur : carte non utilisable (état = " + lifeCycleState + ").");
            return false;
        }

        if (userTriesLeft <= 0) {
            lifeCycleState = LCS.BLOCKED;
            System.out.println("Carte bloquée : plus d'essais utilisateur restants.");
            return false;
        }

        try {
            int[] enteredPIN = readPINFromConsole("utilisateur");
            boolean ok = verifyPINUser(enteredPIN);

            if (ok) {
                userAuthenticate = true;
                userTriesLeft = MAX_USER_TRIES;
                System.out.println("Identification utilisateur réussie.");
                return true;
            } else {
                userTriesLeft--;
                userAuthenticate = false;
                System.out.println("PIN utilisateur incorrect. Essais restants : " + userTriesLeft);
                if (userTriesLeft <= 0) {
                    lifeCycleState = LCS.BLOCKED;
                    System.out.println("Carte bloquée suite à trop d'erreurs de PIN utilisateur.");
                }
                return false;
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Erreur de saisie du PIN utilisateur : " + ex.getMessage());
            return false;
        }
    }

    private boolean getIdentificationAdmin() {
        if (lifeCycleState == LCS.DEAD) {
            System.out.println("Carte morte : impossible d'identifier l'administrateur.");
            return false;
        }

        if (adminTriesLeft <= 0) {
            lifeCycleState = LCS.DEAD;
            System.out.println("Carte morte : plus d'essais administrateur restants.");
            return false;
        }

        try {
            int[] enteredPIN = readPINFromConsole("administrateur");
            boolean ok = verifyPINAdmin(enteredPIN);

            if (ok) {
                adminAuthenticate = true;
                adminTriesLeft = MAX_ADMIN_TRIES; // remise à zéro des essais
                System.out.println("Identification administrateur réussie.");
                return true;
            } else {
                adminTriesLeft--;
                adminAuthenticate = false;
                System.out.println("PIN administrateur incorrect. Essais restants : " + adminTriesLeft);
                if (adminTriesLeft <= 0) {
                    lifeCycleState = LCS.DEAD;
                    System.out.println("Carte morte suite à trop d'erreurs de PIN administrateur.");
                }
                return false;
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Erreur de saisie du PIN administrateur : " + ex.getMessage());
            return false;
        }
    }

    public void PINChangeUnblock() {
        if (lifeCycleState == LCS.DEAD) {
            System.out.println("Carte morte : impossible de débloquer le PIN utilisateur.");
            return;
        }

        System.out.println("Déblocage du PIN utilisateur : identification administrateur requise.");
        boolean ok = getIdentificationAdmin();

        if (!ok) {
            System.out.println("Déblocage échoué (administrateur non identifié).");
            return;
        }

        userTriesLeft = MAX_USER_TRIES;
        if (lifeCycleState == LCS.BLOCKED) {
            lifeCycleState = LCS.USE;
        }
        adminAuthenticate = false;
        System.out.println("PIN utilisateur débloqué. Essais utilisateur remis à " + MAX_USER_TRIES + ".");
    }

    public void beginTransactionDebit(int amount) {
        if (lifeCycleState != LCS.USE) {
            throw new IllegalStateException("Carte non utilisable (état = " + lifeCycleState + ").");
        }
        if (transLeft <= 0) {
            lifeCycleState = LCS.DEAD;
            throw new IllegalStateException("Plus de transactions disponibles (carte morte).");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Montant de débit invalide (doit être > 0).");
        }
        if (amount > MAX_DEBIT_AMOUNT) {
            throw new IllegalArgumentException("Montant de débit supérieur au plafond autorisé : " + MAX_DEBIT_AMOUNT);
        }
        if (balance - amount < 0) {
            throw new IllegalArgumentException("Solde insuffisant pour ce débit (solde actuel = " + balance + ").");
        }

        balance -= amount;
        System.out.println("BEGIN_TRANSACTION_DEBIT : " + amount + " débité (solde provisoire = " + balance + ").");
    }

    public void beginTransactionCredit(int amount) {
        if (lifeCycleState != LCS.USE) {
            throw new IllegalStateException("Carte non utilisable (état = " + lifeCycleState + ").");
        }
        if (transLeft <= 0) {
            lifeCycleState = LCS.DEAD;
            throw new IllegalStateException("Plus de transactions disponibles (carte morte).");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Montant de crédit invalide (doit être > 0).");
        }
        if (amount > MAX_CREDIT_AMOUNT) {
            throw new IllegalArgumentException("Montant de crédit supérieur au plafond autorisé : " + MAX_CREDIT_AMOUNT);
        }
        if (balance + amount > MAX_BALANCE) {
            throw new IllegalArgumentException("Crédit impossible : dépassement du solde maximal (" + MAX_BALANCE + ").");
        }

        System.out.println("BEGIN_TRANSACTION_CREDIT : identification utilisateur requise.");
        boolean ok = getIdentificationUser();

        if (!ok) {
            System.out.println("Crédit annulé : utilisateur non identifié.");
            return;
        }

        balance += amount;
        System.out.println("Crédit de " + amount + " effectué (solde provisoire = " + balance + ").");
    }

    public void commitTransactionDebit() {
        if (lifeCycleState != LCS.USE) {
            throw new IllegalStateException("Carte non utilisable (état = " + lifeCycleState + ").");
        }
        if (transLeft <= 0) {
            lifeCycleState = LCS.DEAD;
            throw new IllegalStateException("Plus de transactions disponibles (carte morte).");
        }

        transLeft--;
        System.out.println("COMMIT_TRANSACTION_DEBIT : transaction validée. Transactions restantes : " + transLeft);

        if (transLeft == 0) {
            lifeCycleState = LCS.DEAD;
            System.out.println("Nombre maximal de transactions atteint : carte maintenant morte.");
        }
    }

    public void commitTransactionCredit() {
        if (lifeCycleState != LCS.USE) {
            throw new IllegalStateException("Carte non utilisable (état = " + lifeCycleState + ").");
        }
        if (transLeft <= 0) {
            lifeCycleState = LCS.DEAD;
            throw new IllegalStateException("Plus de transactions disponibles (carte morte).");
        }

        transLeft--;
        userAuthenticate = false;
        System.out.println("COMMIT_TRANSACTION_CREDIT : transaction validée. Transactions restantes : " + transLeft);

        if (transLeft == 0) {
            lifeCycleState = LCS.DEAD;
            System.out.println("Nombre maximal de transactions atteint : carte maintenant morte.");
        }
    }

    public int getData() {
        return balance;
    }

    public LCS getLifeCycleState() {
        return lifeCycleState;
    }

    public int getTransLeft() {
        return transLeft;
    }

    public int getUserTriesLeft() {
        return userTriesLeft;
    }

    public int getAdminTriesLeft() {
        return adminTriesLeft;
    }
}