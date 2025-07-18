package dao;

import org.ToDo.ListaUtenti;

/**
 * Interfaccia per la gestione della persistenza dei dati relativi alle liste di utenti condivisi e ai contatti.
 * Definisce i metodi per aggiungere, recuperare e rimuovere le associazioni tra ToDo e utenti,
 * e per gestire la lista dei contatti di un utente.
 */
public interface ListaUtentiDAO {

    /**
     * Aggiunge un utente alla lista di condivisione di un ToDo specifico.
     * Se l'utente è già presente nella lista, l'operazione non ha effetto.
     *
     * @param todoId L'ID del ToDo a cui aggiungere l'utente.
     * @param email L'email dell'utente da aggiungere alla lista di condivisione.
     */
    void addUserToSharedList(int todoId, String email);

    /**
     * Recupera la lista degli utenti con cui un ToDo è condiviso.
     *
     * @param todoId L'ID del ToDo di cui recuperare la lista di utenti condivisi.
     * @param autoreEmail L'email dell'autore del ToDo.
     * @return Un oggetto ListaUtenti contenente l'autore e la lista delle email degli utenti condivisi.
     */
    ListaUtenti getSharedUsersForToDo(int todoId, String autoreEmail);

    /**
     * Recupera la lista dei contatti di un utente specifico.
     *
     * @param utenteEmail L'email dell'utente di cui recuperare i contatti.
     * @return Un oggetto ListaUtenti contenente i contatti dell'utente.
     */
    ListaUtenti getContattiForUser(String utenteEmail);

    /**
     * Aggiunge un nuovo contatto alla lista di un utente.
     *
     * @param utenteEmail L'email dell'utente a cui aggiungere il contatto.
     * @param contattoEmail L'email del contatto da aggiungere.
     */
    void aggiungiContatto(String utenteEmail, String contattoEmail);

    /**
     * Rimuove un contatto dalla lista di un utente.
     *
     * @param utenteEmail L'email dell'utente da cui rimuovere il contatto.
     * @param contattoEmail L'email del contatto da rimuovere.
     */
    void rimuoviContatto(String utenteEmail, String contattoEmail);
}