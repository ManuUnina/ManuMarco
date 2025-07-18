package org.ToDo;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta una bacheca tematica che contiene una collezione di attività (ToDo).
 * Ogni bacheca è associata a un utente specifico e ha un titolo e una descrizione.
 * Fornisce metodi per gestire la lista di ToDo al suo interno.
 */
public class Bacheca {
    /**
     * Il titolo tematico della bacheca, definito dall'enumerazione {@link Titolo}.
     * Può essere null per bacheche speciali, come quella dei ToDo condivisi.
     */
    private Titolo titolo;
    /**
     * Una descrizione testuale della bacheca.
     */
    private String descrizione;
    /**
     * L'email dell'utente proprietario della bacheca.
     */
    private String utenteEmail;
    /**
     * La lista di oggetti {@link ToDo} contenuti in questa bacheca.
     */
    private List<ToDo> toDos;

    /**
     * Costruttore per creare una nuova istanza di Bacheca.
     *
     * @param titolo Il titolo della bacheca (può essere null).
     * @param descrizione La descrizione della bacheca.
     * @param utenteEmail L'email dell'utente proprietario.
     */
    public Bacheca(Titolo titolo, String descrizione, String utenteEmail) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.utenteEmail = utenteEmail;
        this.toDos = new ArrayList<>();
    }

    /**
     * Restituisce l'email dell'utente proprietario della bacheca.
     *
     * @return l'email dell'utente.
     */
    public String getUtenteEmail() {
        return utenteEmail;
    }

    /**
     * Restituisce il titolo della bacheca.
     *
     * @return il {@link Titolo} della bacheca.
     */
    public Titolo getTitolo() {
        return titolo;
    }

    /**
     * Imposta o aggiorna la descrizione della bacheca.
     *
     * @param descrizione la nuova descrizione.
     */
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    /**
     * Restituisce la descrizione della bacheca.
     *
     * @return la descrizione testuale.
     */
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Restituisce la lista di ToDo contenuti nella bacheca.
     *
     * @return una {@link List} di oggetti {@link ToDo}.
     */
    public List<ToDo> getToDos() {
        return toDos;
    }

    /**
     * Sostituisce l'intera lista di ToDo della bacheca con una nuova lista.
     *
     * @param toDos la nuova lista di ToDo.
     */
    public void setToDos(List<ToDo> toDos) {
        this.toDos = toDos;
    }

    /**
     * Aggiunge un singolo ToDo alla lista della bacheca.
     *
     * @param todo l'oggetto {@link ToDo} da aggiungere.
     */
    public void aggiungiToDo(ToDo todo) {
        toDos.add(todo);
    }

    /**
     * Rimuove un ToDo dalla bacheca basandosi sulla sua posizione (indice) nella lista.
     *
     * @param indice l'indice del ToDo da rimuovere.
     */
    public void rimuoviToDo(int indice) {
        if (indice >= 0 && indice < toDos.size()) {
            toDos.remove(indice);
        }
    }

    /**
     * Restituisce una rappresentazione testuale della bacheca.
     * Se il titolo è null (come nel caso della bacheca condivisa), restituisce "Bacheca Condivisa".
     * Altrimenti, restituisce il nome del titolo dell'enum.
     *
     * @return una stringa che rappresenta la bacheca.
     */
    @Override
    public String toString() {
        if (titolo == null) {
            return "Bacheca Condivisa";
        }
        return titolo.name();
    }
}