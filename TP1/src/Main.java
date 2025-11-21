import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        Purse purse = new Purse(
                new int[] {1, 2, 3, 4},
                new int[] {9, 9, 9, 9}
        );

        try {
            // Credit de 30 
            System.out.println("Credit de 30 (PIN user correct)");
            purse.beginTransactionCredit(30);
            purse.commitTransactionCredit();
            System.out.println("Solde : " + purse.getData() + "\n");

            // Débit de 10
            System.out.println("Debit de 10");
            purse.beginTransactionDebit(10);
            purse.commitTransactionDebit();
            System.out.println("Solde : " + purse.getData() + "\n");

            // Blocage de carte
            System.out.println("3 tentatives PIN pour simuler un blocage");

            purse.beginTransactionCredit(5);
            purse.beginTransactionCredit(5);
            purse.beginTransactionCredit(5); //carte bloquee

            System.out.println("Etat de la carte apres blocage : " + purse.getLifeCycleState() + "\n");

            // Utilisation avec carte bloquee
            System.out.println("Tentative de debit alors que carte bloquee");
            try {
                purse.beginTransactionDebit(5);
            } catch (Exception ex) {
                System.out.println("Erreur : " + ex.getMessage());
            }
            System.out.println();

            // Deblocage de la carte via l'admin
            System.out.println("Deblocage via PIN admin (9999)");
            purse.PINChangeUnblock();

            System.out.println("Etat de la carte : " + purse.getLifeCycleState() + "\n");

            // Credit apres deblocage
            System.out.println("Credit de 20 apres deblocage");
            purse.beginTransactionCredit(20);
            purse.commitTransactionCredit();
            System.out.println("Solde : " + purse.getData() + "\n");

            // Epuisement des transactions restantes
            System.out.println("On epuise toutes les transactions restantes...");
            while (purse.getLifeCycleState() == LCS.USE) {
                try {
                    purse.beginTransactionDebit(1);
                    purse.commitTransactionDebit();
                } catch (Exception ex) {
                    System.out.println("Erreur : " + ex.getMessage());
                    break;
                }
            }

            System.out.println("\nEtat final de la carte : " + purse.getLifeCycleState());
            System.out.println("Transactions restantes : " + purse.getTransLeft());
            System.out.println("Solde final : " + purse.getData());

        } catch (Exception e) {
            System.out.println("Une erreur inattendue est survenue : " + e.getMessage());
        }
    }
}
