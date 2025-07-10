package org.ToDo;

import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;

public class ToDo {
    private int id;
    private String titolo;
    private String descrizione;
    private LocalDate scadenza;
    private Boolean stato;
    private String url;
    private String posizione; // Aggiunto
    private Color colore;
    private byte[] immagine;
    private Titolo bachecaTitolo;
    private ListaUtenti listaUtenti;

    public ToDo(String titolo, String descrizione, LocalDate scadenza, Boolean stato, String url, String posizione, Color colore, byte[] immagine, Titolo bachecaTitolo, String autoreEmail) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.scadenza = scadenza;
        this.stato = stato;
        this.url = url;
        this.posizione = posizione; // Aggiunto
        this.colore = colore;
        this.immagine = immagine;
        this.bachecaTitolo = bachecaTitolo;
        this.listaUtenti = new ListaUtenti(autoreEmail, new ArrayList<>());
    }

    // Getters
    public int getId() { return id; }
    public String getTitolo() { return titolo; }
    public String getDescrizione() { return descrizione; }
    public LocalDate getScadenza() { return scadenza; }
    public Boolean getStato() { return stato; }
    public String getUrl() { return url; }
    public String getPosizione() { return posizione; } // Aggiunto
    public Color getColore() { return colore; }
    public byte[] getImmagine() { return immagine; }
    public Titolo getBachecaTitolo() { return bachecaTitolo; }
    public ListaUtenti getListaUtenti() { return listaUtenti; }

    public String getAutoreEmail() {
        return (listaUtenti != null) ? listaUtenti.getAutore() : null;
    }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitolo(String titolo) { this.titolo = titolo; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public void setScadenza(LocalDate scadenza) { this.scadenza = scadenza; }
    public void setStato(Boolean stato) { this.stato = stato; }
    public void setUrl(String url) { this.url = url; }
    public void setPosizione(String posizione) { this.posizione = posizione; } // Aggiunto
    public void setColore(Color colore) { this.colore = colore; }
    public void setImmagine(byte[] immagine) { this.immagine = immagine; }
    public void setBachecaTitolo(Titolo bachecaTitolo) { this.bachecaTitolo = bachecaTitolo; }
    public void setListaUtenti(ListaUtenti listaUtenti) { this.listaUtenti = listaUtenti; }
}