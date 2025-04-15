package org.ToDo;
import java.util.Scanner;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
/*public class Main {
    public static void main(String[] args) {
        //Utente a = new Utente(email, password);
        Scanner sc = new Scanner(System.in);

        System.out.print("Inserire email valida: ");
        //a.setEmail("marco");
        String email = sc.nextLine();

        System.out.print("\nInserire password valida: ");
        //a.setPassword("123");
        String password = sc.nextLine();

        //String email = sc.nextLine();
        //String password =sc.nextLine();


        Utente a = new Utente(email, password);
        System.out.println(a.stampa());


        System.out.println("Scegli un titolo tra i seguenti:");
        for (Titolo t : Titolo.values()) {
            System.out.println("- " + t);
        }

        Titolo titolo;
        System.out.println("\ninserire titolo:");
        Titolo intitolo = sc.nextLine();
        titolo=intitolo;
        titolo = Titolo.valueOf(intitolo);


        System.out.print("\ninserire descrizione:");
        String descrizione = sc.nextLine();


        Bacheca b = new Bacheca(titolo, descrizione);
        System.out.println(b.stampa());



    }
}*/

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Utente utenteRegistrato = new Utente("manu@email.com", "cane123");

        System.out.println("Sei già registrato? (s/n)\n");
        String risposta = sc.nextLine();

        if(risposta.equalsIgnoreCase("s")) {
            if(utenteRegistrato == null) {
                utenteRegistrato=new Utente("esempio@email.com", "password123");
            }

            System.out.print("Inserisci un email:\n");
            String email = sc.nextLine();

            System.out.print("Inserisci la password:\n");
            String password = sc.nextLine();

            if(utenteRegistrato.verificaCredenziali(email, password)) {
                System.out.println("Registrato correttamente\n");
            }else {
                System.out.println("Errore\n");
            }
        }else{
            System.out.print("Inserisci un email per registrarti:\n");
            String nuovaEmail = sc.nextLine();

            System.out.print("Inserisci la password:\n");
            String nuovapPassword = sc.nextLine();

            System.out.println("Registrazione completata\n");
        }
        sc.close();
    }
}