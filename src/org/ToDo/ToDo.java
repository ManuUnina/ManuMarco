package org.ToDo;

import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Rappresenta una singola attività (ToDo) all'interno dell'applicazione.
 * Contiene tutti gli attributi necessari per descrivere un'attività, come titolo,
 * descrizione, scadenza, stato di completamento e altri dettagli opzionali
 * come URL, posizione, colore e immagine. Gestisce anche la lista di utenti
 * con cui l'attività è condivisa.
 */
public class ToDo {
    /**
     * L'identificatore univoco del ToDo, generato dal database.
     */
    private int id;
    /**
     * Il titolo del ToDo.
     */
    private String titolo;
    /**
     * Una descrizione dettagliata del ToDo.
     */
    private String descrizione;
    /**
     * La data di scadenza del ToDo.
     */
    private LocalDate scadenza;
    /**
     * Lo stato di completamento del ToDo (true se completato, false altrimenti).
     */
    private Boolean stato;
    /**
     * Un URL associato al ToDo (opzionale).
     */
    private String url;
    /**
     * Una posizione geografica o fisica associata al ToDo (opzionale).
     */
    private String posizione;
    /**
     * Un colore per personalizzare la visualizzazione del ToDo (opzionale).
     */
    private Color colore;
    /**
     * Un'immagine allegata al ToDo, memorizzata come array di byte (opzionale).
     */
    private byte[] immagine;
    /**
     * Il titolo della bacheca a cui questo ToDo appartiene.
     */
    private Titolo bachecaTitolo;
    /**
     * La lista degli utenti con cui questo ToDo è condiviso, gestita tramite l'oggetto ListaUtenti.
     */
    private ListaUtenti listaUtenti;

    /**
     * Costruttore per creare una nuova istanza di ToDo.
     *
     * @param titolo Il titolo del ToDo.
     * @param descrizione La descrizione del ToDo.
     * @param scadenza La data di scadenza.
     * @param stato Lo stato di completamento iniziale.
     * @param url L'URL associato.
     * @param posizione La posizione associata.
     * @param colore Il colore per la visualizzazione.
     * @param immagine L'immagine allegata come array di byte.
     * @param bachecaTitolo Il titolo della bacheca di appartenenza.
     * @param autoreEmail L'email dell'utente che ha creato il ToDo, che sarà l'autore iniziale.
     */
    public ToDo(String titolo, String descrizione, LocalDate scadenza, Boolean stato, String url, String posizione, Color colore, byte[] immagine, Titolo bachecaTitolo, String autoreEmail) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.scadenza = scadenza;
        this.stato = stato;
        this.url = url;
        this.posizione = posizione;
        this.colore = colore;
        this.immagine = immagine;
        this.bachecaTitolo = bachecaTitolo;
        this.listaUtenti = new ListaUtenti(autoreEmail, new ArrayList<>());
    }

    // Getters
    /** @return l'ID del ToDo. */
    public int getId() { return id; }
    /** @return il titolo del ToDo. */
    public String getTitolo() { return titolo; }
    /** @return la descrizione del ToDo. */
    public String getDescrizione() { return descrizione; }
    /** @return la data di scadenza del ToDo. */
    public LocalDate getScadenza() { return scadenza; }
    /** @return lo stato di completamento del ToDo. */
    public Boolean getStato() { return stato; }
    /** @return l'URL associato al ToDo. */
    public String getUrl() { return url; }
    /** @return la posizione associata al ToDo. */
    public String getPosizione() { return posizione; }
    /** @return il colore associato al ToDo. */
    public Color getColore() { return colore; }
    /** @return l'immagine allegata come array di byte. */
    public byte[] getImmagine() { return immagine; }
    /** @return il titolo della bacheca di appartenenza. */
    public Titolo getBachecaTitolo() { return bachecaTitolo; }
    /** @return l'oggetto ListaUtenti che gestisce la condivisione. */
    public ListaUtenti getListaUtenti() { return listaUtenti; }

    /**
     * Restituisce l'email dell'autore del ToDo.
     * @return l'email dell'autore, o null se la lista utenti non è inizializzata.
     */
    public String getAutoreEmail() {
        return (listaUtenti != null) ? listaUtenti.getAutore() : null;
    }

    // Setters
    /** @param id il nuovo ID del ToDo. */
    public void setId(int id) { this.id = id; }
    /** @param titolo il nuovo titolo del ToDo. */
    public void setTitolo(String titolo) { this.titolo = titolo; }
    /** @param descrizione la nuova descrizione del ToDo. */
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    /** @param scadenza la nuova data di scadenza del ToDo. */
    public void setScadenza(LocalDate scadenza) { this.scadenza = scadenza; }
    /** @param stato il nuovo stato di completamento del ToDo. */
    public void setStato(Boolean stato) { this.stato = stato; }
    /** @param url il nuovo URL del ToDo. */
    public void setUrl(String url) { this.url = url; }
    /** @param posizione la nuova posizione del ToDo. */
    public void setPosizione(String posizione) { this.posizione = posizione; }
    /** @param colore il nuovo colore del ToDo. */
    public void setColore(Color colore) { this.colore = colore; }
    /** @param immagine la nuova immagine del ToDo come array di byte. */
    public void setImmagine(byte[] immagine) { this.immagine = immagine; }
    /** @param bachecaTitolo il nuovo titolo della bacheca di appartenenza. */
    public void setBachecaTitolo(Titolo bachecaTitolo) { this.bachecaTitolo = bachecaTitolo; }
    /** @param listaUtenti il nuovo oggetto ListaUtenti per la condivisione. */
    public void setListaUtenti(ListaUtenti listaUtenti) { this.listaUtenti = listaUtenti; }
}