package org.ToDo;
import java.util.EnumMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static Map<Titolo, Bacheca> bacheche = new EnumMap<>(Titolo.class);
    public static Scanner sc = new Scanner(System.in);
    public static Utente utenteRegistrato;

    public static void main(String[] args) {
        utenteRegistrato = new Utente("manu", "cane123");

        System.out.println("Sei già registrato? (s/n)\n");
        String risposta = sc.nextLine();

        boolean accessoConsentito = false;

        if (risposta.equalsIgnoreCase("s")) {
            /*if(utenteRegistrato == null) {
                utenteRegistrato=new Utente("esempio@email.com", "password123");
            }*/

            System.out.print("Inserisci un email:\n");
            String email = sc.nextLine();

            System.out.print("Inserisci la password:\n");
            String password = sc.nextLine();

            if (utenteRegistrato.verificaCredenziali(email, password)) {
                System.out.println("Registrato correttamente\n");
                accessoConsentito = true;
            } else {
                System.out.println("Errore\n");
            }
        } else {
            System.out.print("Inserisci un email per registrarti:\n");
            String nuovaEmail = sc.nextLine();

            System.out.print("Inserisci la password:\n");
            String nuovapPassword = sc.nextLine();

            utenteRegistrato = new Utente(nuovaEmail, nuovapPassword);
            System.out.println("Registrazione completata\n");
            accessoConsentito = true;
        }
        if (accessoConsentito) {
            Bacheca.inizializzaBacheche();
            boolean esci = false;

            while (!esci) {
                System.out.println("\n--- Gestione ToDo ---");
                System.out.println("1. Visualizza Bacheche");
                System.out.println("2. Aggiungi ToDo");
                System.out.println("3. Modifica ToDo");
                System.out.println("4. Elimina ToDo");
                System.out.println("5. Sposta ToDo tra Bacheche");
                System.out.println("6. Cambia ordine ToDo ");
                System.out.println("7. Aggiungi utenti al ToDo ");
                System.out.println("8. Esci");
                System.out.print("Scelta: ");
                int scelta = Integer.parseInt(sc.nextLine());

                switch (scelta) {
                    case 1 -> Bacheca.visualizzaBacheche();
                    case 2 -> ToDo.aggiungiToDo();
                    case 3 -> ToDo.modificaToDo();
                    case 4 -> ToDo.eliminaToDo();
                    case 5 -> ToDo.spostaToDo();
                    case 6 -> ToDo.cambiaOrdineToDo();
                    case 7 -> ToDo.aggiungiUtenti();
                    case 8 -> esci = true;
                    default -> System.out.println("Errore\n");
                }
            }
        }
        sc.close();
    }

}