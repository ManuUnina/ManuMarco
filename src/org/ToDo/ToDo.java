package org.ToDo;

import java.sql.Blob;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.ToDo.Utente;


public class ToDo{
    public String titolo;
    public LocalDate scadenza;
    public String posizione;
    public String url;
    public String descrizione;
    public Blob immagine;
    public Boolean stato;
    public static ListaUtenti lista;

    public ToDo(String titolo, String descrizione, String autore, LocalDate scadenza, String posizione, /*Blob immagine,*/ Boolean stato, String url, ArrayList<String> lista) {
        this.titolo = titolo;
        this.scadenza = scadenza;
        this.descrizione = descrizione;
        this.posizione = posizione;
        // this.immagine = immagine;
        this.stato = stato;
        this.url = url;
        this.lista= new ListaUtenti(autore, lista);
    }

    public Boolean getStato() {
        return stato;
    }

    public Blob getImmagine() {
        return immagine;
    }

    public String getUrl() {
        return url;
    }

    public String getPosizione() {
        return posizione;
    }

    public LocalDate getScadenza() {
        return scadenza;
    }

    public String getTitolo(){
        return titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public ListaUtenti getLista() {
        return lista;
    }
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public void setStato(Boolean stato) {
        this.stato = stato;
    }

    public void setImmagine(Blob immagine) {
        this.immagine = immagine;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPosizione(String posizione) {
        this.posizione = posizione;
    }

    public void setScadenza(LocalDate scadenza) {
        this.scadenza = scadenza;
    }

    @Override
    public String toString() {
        return "ToDo: " + titolo + " - " + descrizione + "\n" + lista.toString();
    }

    public static void aggiungiToDo() {
        //scelta bacheca
        Titolo titolo = Bacheca.scegliBacheca();

        //assegnazione titolo
        System.out.print("Titolo ToDo: ");
        String titoloToDo = Main.sc.nextLine();

        //assegnazione descrizione
        System.out.print("Descrizione ToDo: ");
        String descrizione = Main.sc.nextLine();

        //autore
        String autore = Main.utenteRegistrato.getEmail();

        //assegnazine data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate scadenza = null;
        boolean dataValida = false;

        while (!dataValida) {
            System.out.print("Data scadenza (formato gg/mm/aaaa): ");
            String input = Main.sc.nextLine();

            try {
                scadenza = LocalDate.parse(input, formatter);
                dataValida = true;
            } catch (DateTimeParseException e) {
                System.out.println("Formato data non valido. Riprova.");
            }
        }

        // assegnazione posizione
        System.out.print("inserisci posizione: ");
        String posizione = Main.sc.nextLine();

        //inserimento url
        String url = "";
        Pattern urlPattern = Pattern.compile("^(https?|ftp)://[\\w.-]+(?:\\.[\\w\\.-]+)+(?:[/\\w\\.-]*)*/?$");
        while (true) {
            System.out.print("URL di riferimento (opzionale, premi invio per saltare): ");
            url = Main.sc.nextLine();
            if (url.isEmpty() || urlPattern.matcher(url).matches()) break;
            System.out.println("URL non valido. Riprova.");
        }

        //inserimento utenti
        String email="";
        Pattern emailPattern= Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
        System.out.println("Inserisci le email degli utenti da aggiungere (scrivi 'fine' per terminare)");
        ArrayList<String> lista = new ArrayList<String>();
        while (true) {
            System.out.print("Email: ");
            email = Main.sc.nextLine();
            if (email.equalsIgnoreCase("fine")) {
                break;
            }else if (email.isEmpty() || emailPattern.matcher(email).matches()) {
                lista.add(email);
                System.out.println("Utente aggiunto.");
            }else {
                System.out.println("Email non valido. Riprova.");}
        }

        Boolean stato = false;
        Main.bacheche.get(titolo).aggiungiToDo(new ToDo(titoloToDo, descrizione, autore, scadenza, posizione,stato, url, lista));
    }

    static void modificaToDo() {
        Titolo titolo = Bacheca.scegliBacheca();
        Bacheca b = Main.bacheche.get(titolo);
        Bacheca.visualizzaBacheca(titolo);
        System.out.print("Indice ToDo da modificare: ");
        int indice = Integer.parseInt(Main.sc.nextLine());

        boolean esci=false;
        while (!esci) {
            System.out.println("\n--- Gestione modifica ---");
            System.out.println("1. modifica titolo");
            System.out.println("2. modifica descrizione");
            System.out.println("3. modifica data scadenza");
            System.out.println("4. modifica posizione");
            System.out.println("5. modifica url");
            System.out.println("6. modifica stato");
            System.out.println("7. aggiungi utenti");
            System.out.println("8. rimozione utenti");
            System.out.println("9. Esci");
            System.out.print("Scelta: ");
            int scelta = Integer.parseInt(Main.sc.nextLine());

            switch (scelta) {
                case 1:
                    System.out.print("Nuovo titolo: ");
                    String nuovoTitolo = Main.sc.nextLine();
                    b.getToDos().get(indice).setTitolo(nuovoTitolo);
                    break;

                case 2:
                    System.out.print("Nuova descrizione: ");
                    String nuovaDescrizione = Main.sc.nextLine();
                    b.getToDos().get(indice).setDescrizione(nuovaDescrizione);
                    break;
                case 3:
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate scadenza = null;
                    boolean dataValida = false;

                    while (!dataValida) {
                        System.out.print("Nuova data scadenza (formato gg/mm/aaaa): ");
                        String input = Main.sc.nextLine();

                        try {
                            scadenza = LocalDate.parse(input, formatter);
                            dataValida = true;
                        } catch (DateTimeParseException e) {
                            System.out.println("Formato data non valido. Riprova.");
                        }
                    }
                    b.getToDos().get(indice).setScadenza(scadenza);
                    break;
                case 4:
                    System.out.print("inserisci nuova posizione: ");
                    String posizione = Main.sc.nextLine();
                    b.getToDos().get(indice).setPosizione(posizione);
                    break;
                case 5:
                    String url = "";
                    Pattern urlPattern = Pattern.compile("^(https?|ftp)://[\\w.-]+(?:\\.[\\w\\.-]+)+(?:[/\\w\\.-]*)*/?$");
                    while (true) {
                        System.out.print("Nuovo URL di riferimento: ");
                        url = Main.sc.nextLine();
                        if (urlPattern.matcher(url).matches()) break;
                        System.out.println("URL non valido. Riprova.");
                    }
                    b.getToDos().get(indice).setUrl(url);
                    break;
                case 6:
                    System.out.print("inserisci (completato) per completare il ToDo: ");
                    String stato = Main.sc.nextLine();
                    if (stato.equalsIgnoreCase("completato")){
                        b.getToDos().get(indice).setStato(true);
                    }
                    break;
                case 8:
                    String email="";
                    Pattern emailPattern= Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
                    System.out.println("Inserisci le email degli utenti da eliminare (scrivi 'fine' per terminare)");
                    ListaUtenti lista=null;
                    lista=b.getToDos().get(indice).getLista();
                    while (true) {
                        System.out.print("Email: ");
                        email = Main.sc.nextLine();
                        if (email.equalsIgnoreCase("fine")) {
                            break;
                        }else if (email.isEmpty() || emailPattern.matcher(email).matches()) {
                            lista.rimuovi(email);
                            System.out.println("Utente rimosso.");
                        }else {
                            System.out.println("Email non valido. Riprova.");}
                    }
                    break;
                case 7:
                    String email2="";
                    Pattern emailPattern2= Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
                    System.out.println("Inserisci le email degli utenti da aggiungere (scrivi 'fine' per terminare)");
                    ListaUtenti lista2=null;
                    lista2=b.getToDos().get(indice).getLista();
                    while (true) {
                        System.out.print("Email: ");
                        email2 = Main.sc.nextLine();
                        if (email2.equalsIgnoreCase("fine")) {
                            break;
                        }else if (email2.isEmpty() || emailPattern2.matcher(email2).matches()) {
                            lista2.aggiungiUtente(email2);
                            System.out.println("Utente rimosso.");
                        }else {
                            System.out.println("Email non valido. Riprova.");}
                    }
                    break;
                case 9:
                    esci = true;
                    break;
                default:
                    System.out.println("Errore\n");
            }
        }
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