package dao;

import org.ToDo.Bacheca;
import org.ToDo.Titolo;

import java.util.Map;

/**
 * Interfaccia per la gestione della persistenza dei dati relativi alle bacheche.
 * Definisce i metodi per trovare, salvare e aggiornare le bacheche degli utenti.
 */
public interface BachecaDAO {

    /**
     * Trova tutte le bacheche associate a un utente specifico.
     *
     * @param utenteEmail L'email dell'utente di cui trovare le bacheche.
     * @return una mappa che associa ogni Titolo di bacheca all'oggetto Bacheca corrispondente.
     */
    Map<Titolo, Bacheca> findAllForUser(String utenteEmail);

    /**
     * Salva una nuova bacheca nel database.
     *
     * @param bacheca L'oggetto Bacheca da salvare.
     */
    void save(Bacheca bacheca);

    /**
     * Aggiorna la descrizione di una bacheca specifica per un dato utente.
     *
     * @param titolo Il titolo della bacheca da aggiornare.
     * @param descrizione La nuova descrizione per la bacheca.
     * @param utenteEmail L'email dell'utente proprietario della bacheca.
     */
    void updateDescrizione(Titolo titolo, String descrizione, String utenteEmail);
}