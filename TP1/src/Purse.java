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
        System.out.print("Veuillez saisir le PIN " + label + " : ");
        String line = scanner.nextLine().trim();

        if (line.isEmpty()) {
            throw new IllegalArgumentException("Le code PIN ne peut pas etre vide");
        }

        int[] pin = new int[line.length()];
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (!Character.isDigit(c)) {
                throw new IllegalArgumentException("Le code PIN ne peut contenur que des chiffres");
            }
            pin[i] = c - '0';
        }
        return pin;
    }

    private boolean getIdentificationUser() {
        if (lifeCycleState != LCS.USE) {
            System.out.println("Impossible d'identifier l'utilisateur (" + lifeCycleState + ")");
            return false;
        }

        if (userTriesLeft <= 0) {
            lifeCycleState = LCS.BLOCKED;
            System.out.println("Carte bloquee !");
            return false;
        }

        try {
            int[] enteredPIN = readPINFromConsole("utilisateur");
            boolean ok = verifyPINUser(enteredPIN);

            if (ok) {
                userAuthenticate = true;
                userTriesLeft = MAX_USER_TRIES;
                System.out.println("Identification reussie");
                return true;
            } else {
                userTriesLeft--;
                userAuthenticate = false;
                System.out.println("PIN incorrect, essais restants : " + userTriesLeft);
                if (userTriesLeft <= 0) {
                    lifeCycleState = LCS.BLOCKED;
                    System.out.println("Vous avez fait trop d'erreur, la carte devient bloquee");
                }
                return false;
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Erreur de saisie du code PIN : " + ex.getMessage());
            return false;
        }
    }

    private boolean getIdentificationAdmin() {
        if (lifeCycleState == LCS.DEAD) {
            System.out.println("Carte morte : impossible d'identifier l'administrateur");
            return false;
        }

        if (adminTriesLeft <= 0) {
            lifeCycleState = LCS.DEAD;
            System.out.println("Carte morte : plus d'essais administrateur restants");
            return false;
        }

        try {
            int[] enteredPIN = readPINFromConsole("administrateur");
            boolean ok = verifyPINAdmin(enteredPIN);

            if (ok) {
                adminAuthenticate = true;
                adminTriesLeft = MAX_ADMIN_TRIES; 
                System.out.println("Identification administrateur reussie");
                return true;
            } else {
                adminTriesLeft--;
                adminAuthenticate = false;
                System.out.println("Code PIN administrateur incorrect, essais restants : " + adminTriesLeft);
                if (adminTriesLeft <= 0) {
                    lifeCycleState = LCS.DEAD;
                    System.out.println("Carte morte");
                }
                return false;
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("Erreur de saisie du codePIN administrateur : " + ex.getMessage());
            return false;
        }
    }

    public void PINChangeUnblock() {
        if (lifeCycleState == LCS.DEAD) {
            System.out.println("Carte morte : impossible de debloquer la carte");
            return;
        }

        System.out.println("Deblocage : identification administrateur requise");
        boolean ok = getIdentificationAdmin();

        if (!ok) {
            System.out.println("Deblocage echoue");
            return;
        }

        userTriesLeft = MAX_USER_TRIES;
        if (lifeCycleState == LCS.BLOCKED) {
            lifeCycleState = LCS.USE;
        }
        adminAuthenticate = false;
        System.out.println("PIN utilisateur debloque, essais utilisateur remis a " + MAX_USER_TRIES);
    }

    public void beginTransactionDebit(int amount) {
        if (lifeCycleState != LCS.USE) {
            throw new IllegalStateException("Carte non utilisable (" + lifeCycleState + ")");
        }
        if (transLeft <= 0) {
            lifeCycleState = LCS.DEAD;
            throw new IllegalStateException("Plus de transactions disponibles");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Montant de debit negatif ou nul");
        }
        if (amount > MAX_DEBIT_AMOUNT) {
            throw new IllegalArgumentException("Montant de debit superieur au plafond : " + MAX_DEBIT_AMOUNT);
        }
        if (balance - amount < 0) {
            throw new IllegalArgumentException("Solde insuffisant pour ce debit (solde actuel = " + balance + ")");
        }

        balance -= amount;
        System.out.println(amount + " debite (solde provisoire = " + balance + ")");
    }

    public void beginTransactionCredit(int amount) {
        if (lifeCycleState != LCS.USE) {
            throw new IllegalStateException("Carte non utilisable (" + lifeCycleState + ")");
        }
        if (transLeft <= 0) {
            lifeCycleState = LCS.DEAD;
            throw new IllegalStateException("Plus de transactions disponibles");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Montant de credit negatif ou nul");
        }
        if (amount > MAX_CREDIT_AMOUNT) {
            throw new IllegalArgumentException("Montant de credit superieur au plafond : " + MAX_CREDIT_AMOUNT);
        }
        if (balance + amount > MAX_BALANCE) {
            throw new IllegalArgumentException("Credit impossible : depassement du solde max (" + MAX_BALANCE + ")");
        }

        System.out.println("Identification utilisateur requise");
        boolean ok = getIdentificationUser();

        if (!ok) {
            System.out.println("Credit annule : utilisateur non identifie");
            return;
        }

        balance += amount;
        System.out.println("Credit de " + amount + " effectue (solde provisoire = " + balance + ")");
    }

    public void commitTransactionDebit() {
        if (lifeCycleState != LCS.USE) {
            throw new IllegalStateException("Carte non utilisable (" + lifeCycleState + ")");
        }
        if (transLeft <= 0) {
            lifeCycleState = LCS.DEAD;
            throw new IllegalStateException("Plus de transactions disponibles");
        }

        transLeft--;
        System.out.println("Transaction validee. Transactions restantes : " + transLeft);

        if (transLeft == 0) {
            lifeCycleState = LCS.DEAD;
            System.out.println("Nombre max de transactions atteint");
        }
    }

    public void commitTransactionCredit() {
        if (lifeCycleState != LCS.USE) {
            throw new IllegalStateException("Carte non utilisable (" + lifeCycleState + ")");
        }
        if (transLeft <= 0) {
            lifeCycleState = LCS.DEAD;
            throw new IllegalStateException("Plus de transactions disponibles");
        }

        transLeft--;
        userAuthenticate = false;
        System.out.println("Transaction validee. Transactions restantes : " + transLeft);

        if (transLeft == 0) {
            lifeCycleState = LCS.DEAD;
            System.out.println("Nombre max de transactions atteint");
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