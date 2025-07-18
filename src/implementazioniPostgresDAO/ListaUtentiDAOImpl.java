package implementazioniPostgresDAO;

import dao.ListaUtentiDAO;
import org.ToDo.ListaUtenti;
import util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione dell'interfaccia {@link ListaUtentiDAO} per il database PostgreSQL.
 * Fornisce metodi concreti per interagire con la tabella dei contatti e la tabella
 * di associazione 'todo_utenti_condivisi' per gestire la condivisione dei ToDo
 * e le liste di contatti degli utenti.
 */
public class ListaUtentiDAOImpl implements ListaUtentiDAO {

    /**
     * Logger per la registrazione di errori e informazioni.
     */
    private static final Logger LOGGER = Logger.getLogger(ListaUtentiDAOImpl.class.getName());

    /**
     * Aggiunge un utente alla lista di condivisione di un ToDo specifico nel database.
     * Utilizza una clausola "ON CONFLICT DO NOTHING" per evitare errori in caso di inserimenti duplicati.
     *
     * @param todoId L'ID del ToDo a cui associare l'utente.
     * @param email L'email dell'utente da aggiungere alla lista di condivisione.
     */
    @Override
    public void addUserToSharedList(int todoId, String email) {
        String sql = "INSERT INTO todo_utenti_condivisi (todo_id, utente_email) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, todoId);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'aggiunta dell'utente alla lista condivisa.", e);
        }
    }

    /**
     * Recupera dal database la lista degli utenti con cui un ToDo è condiviso.
     *
     * @param todoId L'ID del ToDo di cui recuperare la lista di condivisione.
     * @param autoreEmail L'email dell'autore del ToDo, usata per costruire l'oggetto {@link ListaUtenti}.
     * @return Un oggetto {@link ListaUtenti} contenente l'autore e la lista delle email degli utenti condivisi.
     */
    @Override
    public ListaUtenti getSharedUsersForToDo(int todoId, String autoreEmail) {
        ArrayList<String> sharedUsers = new ArrayList<>();
        String sql = "SELECT utente_email FROM todo_utenti_condivisi WHERE todo_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, todoId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sharedUsers.add(rs.getString("utente_email"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero degli utenti condivisi per il ToDo ID: " + todoId, e);
        }
        return new ListaUtenti(autoreEmail, sharedUsers);
    }

    /**
     * Recupera dal database la lista dei contatti di un utente specifico.
     *
     * @param utenteEmail L'email dell'utente di cui si vogliono recuperare i contatti.
     * @return Un oggetto {@link ListaUtenti} che funge da contenitore per la lista dei contatti.
     * L'autore in questo contesto è l'utente stesso.
     */
    @Override
    public ListaUtenti getContattiForUser(String utenteEmail) {
        ArrayList<String> contatti = new ArrayList<>();
        String sql = "SELECT contatto_email FROM contatto WHERE utente_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, utenteEmail);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                contatti.add(rs.getString("contatto_email"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero dei contatti per l'utente: " + utenteEmail, e);
        }
        return new ListaUtenti(utenteEmail, contatti);
    }

    /**
     * Aggiunge un nuovo contatto alla lista di un utente nel database.
     * La clausola "ON CONFLICT DO NOTHING" previene l'inserimento di contatti duplicati.
     *
     * @param utenteEmail L'email dell'utente a cui aggiungere il contatto.
     * @param contattoEmail L'email del contatto da aggiungere.
     */
    @Override
    public void aggiungiContatto(String utenteEmail, String contattoEmail) {
        String sql = "INSERT INTO contatto (utente_email, contatto_email) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, utenteEmail);
            pstmt.setString(2, contattoEmail);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'aggiunta del contatto.", e);
        }
    }

    /**
     * Rimuove un contatto dalla lista di un utente nel database.
     *
     * @param utenteEmail L'email dell'utente dalla cui lista si vuole rimuovere il contatto.
     * @param contattoEmail L'email del contatto da rimuovere.
     */
    @Override
    public void rimuoviContatto(String utenteEmail, String contattoEmail) {
        String sql = "DELETE FROM contatto WHERE utente_email = ? AND contatto_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, utenteEmail);
            pstmt.setString(2, contattoEmail);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la rimozione del contatto.", e);
        }
    }
}