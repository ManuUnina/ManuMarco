package org.ToDo;

import java.util.ArrayList;

/**
 * Rappresenta una lista di utenti associati a un contesto specifico, come la condivisione di un ToDo o una lista di contatti.
 * Questa classe incapsula un autore (o proprietario della lista) e un elenco di email di altri utenti.
 */
public class ListaUtenti {
    /**
     * L'email dell'utente che ha creato o che possiede la lista (es. l'autore di un ToDo).
     */
    public String Autore;
    /**
     * La lista di email degli utenti associati.
     */
    public ArrayList<String> Lista;

    /**
     * Costruttore per creare una nuova istanza di ListaUtenti.
     * Crea una copia difensiva della lista passata per garantire l'incapsulamento.
     *
     * @param Autore L'email dell'utente autore/proprietario.
     * @param Lista Un'{@link ArrayList} di stringhe contenente le email degli utenti da associare.
     */
    public ListaUtenti(String Autore, ArrayList<String> Lista) {
        this.Autore=Autore;
        this.Lista=new ArrayList<String>(Lista);
    }

    /**
     * Restituisce la lista di email degli utenti.
     *
     * @return un'{@link ArrayList} di stringhe contenente le email.
     */
    public ArrayList<String> getLista(){
        return Lista;
    }

    /**
     * Restituisce l'email dell'autore/proprietario della lista.
     *
     * @return una stringa che rappresenta l'email dell'autore.
     */
    public String getAutore(){
        return Autore;
    }

    /**
     * Imposta o aggiorna l'email dell'autore della lista.
     *
     * @param Autore la nuova email dell'autore.
     */
    public void setAutore(String Autore){
        this.Autore = Autore;
    }

    /**
     * Aggiunge un utente alla lista, solo se non è già presente.
     * Questo previene duplicati nella lista di condivisione o di contatti.
     *
     * @param email L'email dell'utente da aggiungere.
     */
    public void aggiungiUtente(String email) {
        if (!Lista.contains(email)) {
            Lista.add(email);
        }
    }

    /**
     * Rimuove un utente dalla lista.
     *
     * @param utente L'email dell'utente da rimuovere.
     */
    public void rimuovi(String utente){
        Lista.remove(utente);
    }

    /**
     * Calcola la dimensione (numero di utenti) di una data lista.
     *
     * @param l L'oggetto ListaUtenti di cui calcolare la dimensione.
     * @return il numero di utenti nella lista.
     */
    public int dimensione (ListaUtenti l){
        return Lista.size();
    }

    /**
     * Restituisce una rappresentazione testuale dell'oggetto,
     * mostrando l'autore e la lista di utenti con cui è condiviso.
     *
     * @return una stringa formattata con i dettagli della lista.
     */
    @Override
    public String toString() {
        return "Autore: " + Autore + "\nCondiviso con: " + Lista.toString();
    }
}