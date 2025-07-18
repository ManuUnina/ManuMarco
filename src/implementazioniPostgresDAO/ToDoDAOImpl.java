package implementazioniPostgresDAO;

import dao.ListaUtentiDAO;
import dao.ToDoDAO;
import org.ToDo.Titolo;
import org.ToDo.ToDo;
import util.ConnectionFactory;

import java.awt.Color;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione dell'interfaccia {@link ToDoDAO} per il database PostgreSQL.
 * Questa classe gestisce tutte le operazioni CRUD (Create, Read, Update, Delete) per gli oggetti {@link ToDo},
 * interfacciandosi direttamente con il database.
 */
public class ToDoDAOImpl implements ToDoDAO {

    /**
     * Logger per la registrazione di errori e informazioni.
     */
    private static final Logger LOGGER = Logger.getLogger(ToDoDAOImpl.class.getName());
    /**
     * Istanza di ListaUtentiDAO per gestire le operazioni sulla lista di utenti condivisi.
     */
    private final ListaUtentiDAO listaUtentiDAO = new ListaUtentiDAOImpl();

    /**
     * Metodo helper privato per creare un oggetto ToDo a partire da un ResultSet.
     * Centralizza la logica di creazione dell'oggetto, evitando duplicazioni di codice
     * e facilitando la manutenzione.
     *
     * @param rs Il ResultSet da cui estrarre i dati del ToDo.
     * @return un nuovo oggetto {@link ToDo} popolato con i dati del ResultSet.
     * @throws SQLException se si verifica un errore durante l'accesso ai dati del ResultSet.
     */
    private ToDo createToDoFromResultSet(ResultSet rs) throws SQLException {
        String autoreEmail = rs.getString("autore_email");
        ToDo todo = new ToDo(
                rs.getString("titolo"),
                rs.getString("descrizione"),
                rs.getDate("scadenza").toLocalDate(),
                rs.getBoolean("stato"),
                rs.getString("url"),
                rs.getString("posizione"),
                Color.decode(rs.getString("colore")),
                rs.getBytes("immagine"),
                Titolo.valueOf(rs.getString("bacheca_titolo")),
                autoreEmail
        );
        todo.setId(rs.getInt("id"));
        todo.setListaUtenti(listaUtentiDAO.getSharedUsersForToDo(todo.getId(), autoreEmail));
        return todo;
    }

    /**
     * {@inheritDoc}
     * Recupera una lista di ToDo da una specifica bacheca appartenente a un utente.
     */
    @Override
    public List<ToDo> findByBacheca(Titolo bachecaTitolo, String utenteEmail) {
        List<ToDo> toDos = new ArrayList<>();
        String sql = "SELECT id, titolo, descrizione, scadenza, stato, url, colore, immagine, posizione, bacheca_titolo, autore_email FROM todo WHERE bacheca_titolo = ? AND autore_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, bachecaTitolo, java.sql.Types.OTHER);
            pstmt.setString(2, utenteEmail);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                toDos.add(createToDoFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero dei ToDo per la bacheca", e);
        }
        return toDos;
    }

    /**
     * {@inheritDoc}
     * Salva un nuovo ToDo, inserendolo nella tabella 'todo' e gestendo l'associazione
     * con gli utenti condivisi.
     */
    @Override
    public void save(ToDo todo) {
        String sql = "INSERT INTO todo (titolo, descrizione, scadenza, stato, url, colore, immagine, bacheca_titolo, autore_email, posizione) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, todo.getTitolo());
            pstmt.setString(2, todo.getDescrizione());
            pstmt.setDate(3, Date.valueOf(todo.getScadenza()));
            pstmt.setBoolean(4, todo.getStato());
            pstmt.setString(5, todo.getUrl());
            pstmt.setString(6, String.format("#%06X", todo.getColore().getRGB() & 0xFFFFFF));
            pstmt.setBytes(7, todo.getImmagine());
            pstmt.setObject(8, todo.getBachecaTitolo(), java.sql.Types.OTHER);
            pstmt.setString(9, todo.getAutoreEmail());
            pstmt.setString(10, todo.getPosizione());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                todo.setId(generatedKeys.getInt(1));
            }

            for (String email : todo.getListaUtenti().getLista()) {
                listaUtentiDAO.addUserToSharedList(todo.getId(), email);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio del ToDo", e);
        }
    }

    /**
     * {@inheritDoc}
     * Aggiorna i dati di un ToDo esistente sulla base del suo ID.
     */
    @Override
    public void update(ToDo todo){
        String sql = "UPDATE todo SET titolo = ?, descrizione = ?, scadenza = ?, stato = ?, url = ?, colore = ?, immagine = ?, bacheca_titolo = ?, posizione = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, todo.getTitolo());
            pstmt.setString(2, todo.getDescrizione());
            pstmt.setDate(3, Date.valueOf(todo.getScadenza()));
            pstmt.setBoolean(4, todo.getStato());
            pstmt.setString(5, todo.getUrl());
            pstmt.setString(6, String.format("#%06X", todo.getColore().getRGB() & 0xFFFFFF));
            pstmt.setBytes(7, todo.getImmagine());
            pstmt.setObject(8, todo.getBachecaTitolo(), java.sql.Types.OTHER);
            pstmt.setString(9, todo.getPosizione());
            pstmt.setInt(10, todo.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'aggiornamento del ToDo con ID: " + todo.getId(), e);
        }
    }

    /**
     * {@inheritDoc}
     * Elimina un ToDo dalla tabella 'todo' usando il suo ID.
     */
    @Override
    public void delete(int todoId){
        String sql = "DELETE FROM todo WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, todoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'eliminazione del ToDo con ID: " + todoId, e);
        }
    }

    /**
     * {@inheritDoc}
     * Recupera tutti i ToDo condivisi con un determinato utente, effettuando una JOIN
     * con la tabella di associazione 'todo_utenti_condivisi'.
     */
    @Override
    public List<ToDo> findSharedWithUser(String userEmail) {
        List<ToDo> toDos = new ArrayList<>();
        String sql = "SELECT t.id, t.titolo, t.descrizione, t.scadenza, t.stato, t.url, t.colore, t.immagine, t.posizione, t.bacheca_titolo, t.autore_email FROM todo t JOIN todo_utenti_condivisi s ON t.id = s.todo_id WHERE s.utente_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                toDos.add(createToDoFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero dei ToDo condivisi con l'utente: " + userEmail, e);
        }
        return toDos;
    }
}