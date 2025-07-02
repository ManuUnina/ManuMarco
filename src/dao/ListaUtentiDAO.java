package dao;

import org.ToDo.ListaUtenti;
import util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * DAO per la gestione della persistenza delle condivisioni (classe ListaUtenti).
 * Si occupa esclusivamente della tabella 'todo_utenti_condivisi'.
 */
public class ListaUtentiDAO {

    /**
     * Aggiunge una condivisione di un ToDo con un utente nel database.
     * @param todoId L'ID del ToDo da condividere.
     * @param email L'email dell'utente con cui condividere.
     */
    public void addUserToSharedList(int todoId, String email) {
        // "ON CONFLICT DO NOTHING" evita errori se la condivisione esiste già
        String sql = "INSERT INTO todo_utenti_condivisi (todo_id, utente_email) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, todoId);
            pstmt.setString(2, email);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recupera dal database la lista di utenti con cui un ToDo è condiviso.
     * @param todoId L'ID del ToDo.
     * @param autoreEmail L'email dell'autore originale del ToDo.
     * @return Un oggetto ListaUtenti popolato con gli utenti condivisi.
     */
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
            e.printStackTrace();
        }
        // Crea e restituisce l'oggetto ListaUtenti completo
        return new ListaUtenti(autoreEmail, sharedUsers);
    }
}