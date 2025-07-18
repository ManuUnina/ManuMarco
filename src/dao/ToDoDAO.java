package dao;

import org.ToDo.Titolo;
import org.ToDo.ToDo;

import java.util.List;

/**
 * Interfaccia per la gestione della persistenza dei dati relativi ai ToDo.
 * Definisce i metodi per trovare, salvare, aggiornare ed eliminare i ToDo,
 * nonché per trovare i ToDo condivisi con un utente specifico.
 */
public interface ToDoDAO {

    /**
     * Trova tutti i ToDo associati a una specifica bacheca e a un utente.
     *
     * @param bachecaTitolo Il titolo della bacheca in cui cercare.
     * @param utenteEmail L'email dell'utente proprietario della bacheca.
     * @return una lista di oggetti ToDo trovati.
     */
    List<ToDo> findByBacheca(Titolo bachecaTitolo, String utenteEmail);

    /**
     * Salva un nuovo ToDo nel database.
     *
     * @param todo L'oggetto ToDo da salvare.
     */
    void save(ToDo todo);

    /**
     * Aggiorna un ToDo esistente nel database.
     *
     * @param todo L'oggetto ToDo con i dati aggiornati.
     */
    void update(ToDo todo);

    /**
     * Elimina un ToDo dal database utilizzando il suo ID.
     *
     * @param todoId L'ID del ToDo da eliminare.
     */
    void delete(int todoId);

    /**
     * Trova tutti i ToDo che sono stati condivisi con un utente specifico.
     *
     * @param userEmail L'email dell'utente con cui i ToDo sono condivisi.
     * @return una lista di oggetti ToDo condivisi con l'utente.
     */
    List<ToDo> findSharedWithUser(String userEmail);
}