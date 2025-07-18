package dao;

import org.ToDo.Utente;

/**
 * Interfaccia per la gestione della persistenza dei dati degli utenti.
 * Definisce i metodi per l'autenticazione, la verifica della registrazione
 * e la creazione di nuovi utenti.
 */
public interface UtenteDAO {

    /**
     * Cerca un utente nel database tramite email e password.
     * Utilizzato per il processo di login.
     *
     * @param email L'email dell'utente.
     * @param password La password dell'utente.
     * @return un oggetto Utente se le credenziali sono corrette, altrimenti null.
     */
    Utente findByEmailAndPassword(String email, String password);

    /**
     * Verifica se un'email è già registrata nel database.
     * Utile per prevenire la registrazione di utenti con la stessa email.
     *
     * @param email L'email da verificare.
     * @return true se l'email è già registrata, false altrimenti.
     */
    boolean isEmailRegistered(String email);

    /**
     * Registra un nuovo utente nel database.
     *
     * @param utente L'oggetto Utente contenente i dati del nuovo utente da registrare.
     * @return true se la registrazione è avvenuta con successo, false in caso di errore.
     */
    boolean registraNuovoUtente(Utente utente);
}