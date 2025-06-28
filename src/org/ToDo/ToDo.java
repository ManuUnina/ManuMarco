package org.ToDo;

import java.awt.Color;
import java.sql.Blob;
import java.time.LocalDate;
import java.util.ArrayList;

public class ToDo {
    private int id; // Aggiunto per l'identificativo del DB
    private String titolo;
    private String descrizione;
    private LocalDate scadenza;
    private Boolean stato;
    private String url;
    private Color colore;
    private Titolo bachecaTitolo;
    private String autoreEmail;
    // La lista utenti condivisi può essere gestita separatamente nel DAO
    // o caricata qui se necessario.

    public ToDo(String titolo, String descrizione, LocalDate scadenza, Boolean stato, String url, Color colore, Titolo bachecaTitolo, String autoreEmail) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.scadenza = scadenza;
        this.stato = stato;
        this.url = url;
        this.colore = colore;
        this.bachecaTitolo = bachecaTitolo;
        this.autoreEmail = autoreEmail;
    }

    // Getters
    public int getId() { return id; }
    public String getTitolo() { return titolo; }
    public String getDescrizione() { return descrizione; }
    public LocalDate getScadenza() { return scadenza; }
    public Boolean getStato() { return stato; }
    public String getUrl() { return url; }
    public Color getColore() { return colore; }
    public Titolo getBachecaTitolo() { return bachecaTitolo; }
    public String getAutoreEmail() { return autoreEmail; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitolo(String titolo) { this.titolo = titolo; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public void setScadenza(LocalDate scadenza) { this.scadenza = scadenza; }
    public void setStato(Boolean stato) { this.stato = stato; }
    public void setUrl(String url) { this.url = url; }
    public void setColore(Color colore) { this.colore = colore; }
    public void setBachecaTitolo(Titolo bachecaTitolo) { this.bachecaTitolo = bachecaTitolo; }
    public void setAutoreEmail(String autoreEmail) { this.autoreEmail = autoreEmail; }
}