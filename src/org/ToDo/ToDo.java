package org.ToDo;

import java.sql.Blob;
import java.util.Date;

public class ToDo{
    public String titolo;
    public Date scadenza;
    public String posizione;
    public String url;
    public String descrizione;
    public Blob immagine;
    public Boolean stato;
    public ListaUtenti lista;

    public ToDo(String titolo, String descrizione, String autore){
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.lista= new ListaUtenti(autore);
    }

    public String getTitolo(){
        return titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public ListaUtenti getLista() {
        return lista;
    }

    @Override
    public String toString() {
        return "ToDo: " + titolo + " - " + descrizione + "\n" + lista.toString();
    }

    static void aggiungiToDo() {
        Titolo titolo = Bacheca.scegliBacheca();
        System.out.print("Titolo ToDo: ");
        String titoloToDo = Main.sc.nextLine();
        System.out.print("Descrizione ToDo: ");
        String descrizione = Main.sc.nextLine();
        String autore = Main.utenteRegistrato.getemail();
        Main.bacheche.get(titolo).aggiungiToDo(new ToDo(titoloToDo, descrizione, autore));
    }

    public static void aggiungiUtenti() {
        Titolo titolo = Bacheca.scegliBacheca();
        Bacheca b = Main.bacheche.get(titolo);
        Bacheca.visualizzaBacheca(titolo);
        System.out.print("Indice del ToDo per aggiungere utenti: ");
        int indice = Integer.parseInt(Main.sc.nextLine());
        ToDo t = b.getToDos().get(indice);
        System.out.println("Inserisci le email degli utenti da aggiungere (scrivi 'fine' per terminare):");
        while (true) {
            System.out.print("Email: ");
            String email = Main.sc.nextLine();
            if (email.equalsIgnoreCase("fine")) break;
            t.getLista().aggiungiUtente(email);
            System.out.println("Utente aggiunto.");
        }
    }


    static void modificaToDo() {
        Titolo titolo = Bacheca.scegliBacheca();
        Bacheca b = Main.bacheche.get(titolo);
        Bacheca.visualizzaBacheca(titolo);
        System.out.print("Indice ToDo da modificare: ");
        int indice = Integer.parseInt(Main.sc.nextLine());
        System.out.print("Nuovo titolo: ");
        String nuovoTitolo = Main.sc.nextLine();
        System.out.print("Nuova descrizione: ");
        String nuovaDescrizione = Main.sc.nextLine();
        b.getToDos().get(indice).setTitolo(nuovoTitolo);
        b.getToDos().get(indice).setDescrizione(nuovaDescrizione);
    }

    static void eliminaToDo() {
        Titolo titolo = Bacheca.scegliBacheca();
        Bacheca.visualizzaBacheca(titolo);
        System.out.print("Indice ToDo da eliminare: ");
        int indice = Integer.parseInt(Main.sc.nextLine());
        Main.bacheche.get(titolo).rimuoviToDo(indice);
    }

    static void spostaToDo() {
        System.out.println("-- Bacheca di origine --");
        Titolo origine = Bacheca.scegliBacheca();
        Bacheca.visualizzaBacheca(origine);
        System.out.print("Indice ToDo da spostare: ");
        int indice = Integer.parseInt(Main.sc.nextLine());
        ToDo daSpostare = Main.bacheche.get(origine).getToDos().remove(indice);

        System.out.println("-- Bacheca di destinazione --");
        Titolo destinazione = Bacheca.scegliBacheca();
        Main.bacheche.get(destinazione).aggiungiToDo(daSpostare);
    }

    static void cambiaOrdineToDo() {
        Titolo titolo = Bacheca.scegliBacheca();
        Bacheca.visualizzaBacheca(titolo);
        System.out.print("Indice ToDo da spostare: ");
        int indice = Integer.parseInt(Main.sc.nextLine());
        System.out.print("Nuova posizione: ");
        int nuovaPosizione = Integer.parseInt(Main.sc.nextLine());
        Main.bacheche.get(titolo).spostaToDo(indice, nuovaPosizione);
    }


}