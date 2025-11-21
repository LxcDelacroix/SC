
import java.util.Scanner;

// Luc DELACROIX - Julien BARTHOD

// Fichier associé LCS.java et Purse.java

// code PIN : utilisateur = 1234 ; admin = 9999
public class Main {

    public static void main(String[] args) {
        int[] userPIN = {1, 2, 3, 4};
        int[] adminPIN = {9, 9, 9, 9};

        Purse purse = new Purse(userPIN, adminPIN);
        Scanner sc = new Scanner(System.in);

        boolean running = true;

        while (running) {
            System.out.println("\n======= MENU PURSE =======");
            System.out.println("Etat de la carte : " + purse.getLifeCycleState());
            System.out.println("Solde actuel      : " + purse.getData());
            System.out.println("Transactions left : " + purse.getTransLeft());
            System.out.println("Essais user left  : " + purse.getUserTriesLeft());
            System.out.println("Essais admin left : " + purse.getAdminTriesLeft());
            System.out.println("--------------------------");
            System.out.println("1 - Créditer");
            System.out.println("2 - Débiter");
            System.out.println("3 - Afficher solde");
            System.out.println("4 - Débloquer PIN utilisateur (admin)");
            System.out.println("0 - Quitter");
            System.out.print("Votre choix : ");

            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1": {
                        System.out.print("Montant à créditer : ");
                        int amount = Integer.parseInt(sc.nextLine().trim());
                        purse.beginTransactionCredit(amount);
                        purse.commitTransactionCredit();
                        break;
                    }
                    case "2": {
                        System.out.print("Montant à débiter : ");
                        int amount = Integer.parseInt(sc.nextLine().trim());
                        purse.beginTransactionDebit(amount);
                        purse.commitTransactionDebit();
                        break;
                    }
                    case "3": {
                        System.out.println("Solde actuel : " + purse.getData());
                        break;
                    }
                    case "4": {
                        purse.PINChangeUnblock();
                        break;
                    }
                    case "0": {
                        running = false;
                        break;
                    }
                    default:
                        System.out.println("Choix invalide.");
                }
            } catch (Exception e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }

        System.out.println("Fin du programme.");
    }
}
