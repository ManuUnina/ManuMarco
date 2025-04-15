package org.ToDo;
import java.util.EnumMap;
import java.util.Map;
import java.util.Scanner;

/*import static org.ToDo.Bacheca.inizializzaBacheche;
import static org.ToDo.Bacheca.visualizzaBacheche;
import static org.ToDo.ToDo.*;*/
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
    public static Map<Titolo, Bacheca> bacheche = new EnumMap<>(Titolo.class);
    public static Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {
        Utente utenteRegistrato = new Utente("manu@email.com", "cane123");

        System.out.println("Sei già registrato? (s/n)\n");
        String risposta = sc.nextLine();

        boolean accessoConsentito = false;

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
                accessoConsentito = true;
            }else {
                System.out.println("Errore\n");
            }
        }else{
            System.out.print("Inserisci un email per registrarti:\n");
            String nuovaEmail = sc.nextLine();

            System.out.print("Inserisci la password:\n");
            String nuovapPassword = sc.nextLine();

            utenteRegistrato = new Utente(nuovaEmail, nuovapPassword);
            System.out.println("Registrazione completata\n");
            accessoConsentito = true;
        }
        if(accessoConsentito) {
            Bacheca.inizializzaBacheche();
            boolean esci = false;

            while(!esci) {
                System.out.println("\n--- Gestione ToDo ---");
                System.out.println("1. Visualizza Bacheche");
                System.out.println("2. Aggiungi ToDo");
                System.out.println("3. Modifica ToDo");
                System.out.println("4. Elimina ToDo");
                System.out.println("5. Sposta ToDo tra Bacheche");
                System.out.println("6. Cambia ordine ToDo ");
                System.out.println("7. Esci");
                System.out.print("Scelta: ");
                int scelta = Integer.parseInt(sc.nextLine());

                switch(scelta) {
                    case 1 -> Bacheca.visualizzaBacheche();
                    case 2 -> ToDo.aggiungiToDo();
                    case 3 -> ToDo.modificaToDo();
                    case 4 -> ToDo.eliminaToDo();
                    case 5 -> ToDo.spostaToDo();
                    case 6 -> ToDo.cambiaOrdineToDo();
                    case 7 -> esci = true;
                    default -> System.out.println("Errore\n");
                }
            }
        }
        sc.close();
    }

    /*private static void inizializzaBacheche() {
        bacheche.put(Titolo.UNIVERSITA, new Bacheca(Titolo.UNIVERSITA, "Compiti e lezioni"));
        bacheche.put(Titolo.LAVORO, new Bacheca(Titolo.LAVORO, "Progetti lavorativi"));
        bacheche.put(Titolo.TEMPO_LIBERO, new Bacheca(Titolo.TEMPO_LIBERO, "Hobby e relax"));
    }

    private static void visualizzaBacheche() {
        bacheche.values().forEach(System.out::println);
    }*/

    /*private static void aggiungiToDo() {
        Titolo titolo = scegliBacheca();
        System.out.print("Titolo ToDo: ");
        String titoloToDo = sc.nextLine();
        System.out.print("Descrizione ToDo: ");
        String descrizione = sc.nextLine();
        bacheche.get(titolo).aggiungiToDo(new ToDo(titoloToDo, descrizione));
    }

    private static void modificaToDo() {
        Titolo titolo = scegliBacheca();
        Bacheca b = bacheche.get(titolo);
        visualizzaBacheca(titolo);
        System.out.print("Indice ToDo da modificare: ");
        int indice = Integer.parseInt(sc.nextLine());
        System.out.print("Nuovo titolo: ");
        String nuovoTitolo = sc.nextLine();
        System.out.print("Nuova descrizione: ");
        String nuovaDescrizione = sc.nextLine();
        b.getToDos().get(indice).setTitolo(nuovoTitolo);
        b.getToDos().get(indice).setDescrizione(nuovaDescrizione);
    }

    private static void eliminaToDo() {
        Titolo titolo = scegliBacheca();
        visualizzaBacheca(titolo);
        System.out.print("Indice ToDo da eliminare: ");
        int indice = Integer.parseInt(sc.nextLine());
        bacheche.get(titolo).rimuoviToDo(indice);
    }

    private static void spostaToDo() {
        System.out.println("-- Bacheca di origine --");
        Titolo origine = scegliBacheca();
        visualizzaBacheca(origine);
        System.out.print("Indice ToDo da spostare: ");
        int indice = Integer.parseInt(sc.nextLine());
        ToDo daSpostare = bacheche.get(origine).getToDos().remove(indice);

        System.out.println("-- Bacheca di destinazione --");
        Titolo destinazione = scegliBacheca();
        bacheche.get(destinazione).aggiungiToDo(daSpostare);
    }

    private static void cambiaOrdineToDo() {
        Titolo titolo = scegliBacheca();
        visualizzaBacheca(titolo);
        System.out.print("Indice ToDo da spostare: ");
        int indice = Integer.parseInt(sc.nextLine());
        System.out.print("Nuova posizione: ");
        int nuovaPosizione = Integer.parseInt(sc.nextLine());
        bacheche.get(titolo).spostaToDo(indice, nuovaPosizione);
    }*/

    /*private static Titolo scegliBacheca() {
        System.out.println("Scegli bacheca:");
        for (Titolo titolo : Titolo.values()) {
            System.out.println("- " + titolo);
        }
        System.out.print("Titolo: ");
        return Titolo.valueOf(sc.nextLine().toUpperCase());
    }

    private static void visualizzaBacheca(Titolo titolo) {
        System.out.println(bacheche.get(titolo));
    }*/
}