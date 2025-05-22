package org.ToDo;

import java.awt.Color; // Importa la classe Color
import java.sql.Blob;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.regex.Pattern;
// org.ToDo.Utente non è necessario qui se non usato direttamente nelle funzioni statiche deprecate di ToDo

public class ToDo {
    public String titolo;
    public LocalDate scadenza;
    public String posizione;
    public String url;
    public String descrizione;
    public Blob immagine;
    public Boolean stato;
    public static ListaUtenti lista; // Considerare se 'lista' debba essere statico o di istanza
    private Color colore; // Nuovo campo per il colore

    // Costruttore aggiornato per includere il colore
    public ToDo(String titolo, String descrizione, String autore, LocalDate scadenza, String posizione, /*Blob immagine,*/ Boolean stato, String url, ArrayList<String> listaUtenti, Color colore) {
        this.titolo = titolo;
        this.scadenza = scadenza;
        this.descrizione = descrizione;
        this.posizione = posizione;
        // this.immagine = immagine;
        this.stato = stato;
        this.url = url;
        this.lista = new ListaUtenti(autore, listaUtenti);
        this.colore = colore; // Imposta il colore
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

    public String getTitolo() {
        return titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public ListaUtenti getLista() {
        return lista;
    }

    public Color getColore() { // Getter per il colore
        return colore;
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

    public void setColore(Color colore) { // Setter per il colore
        this.colore = colore;
    }

    @Override
    public String toString() {
        // Il toString standard potrebbe non includere il colore,
        // poiché la visualizzazione del colore avverrà graficamente.
        return "ToDo: " + titolo + " - " + descrizione + "\n" + (lista != null ? lista.toString() : "Nessuna lista utenti");
    }
}