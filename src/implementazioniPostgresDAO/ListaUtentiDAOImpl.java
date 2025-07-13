package implementazioniPostgresDAO;

import dao.ListaUtentiDAO;
import org.ToDo.ListaUtenti;
import util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ListaUtentiDAOImpl implements ListaUtentiDAO {

    @Override
    public void addUserToSharedList(int todoId, String email) {
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
            e.printStackTrace();
        }
        return new ListaUtenti(autoreEmail, sharedUsers);
    }

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
            e.printStackTrace();
        }
        return new ListaUtenti(utenteEmail, contatti);
    }

    @Override
    public void aggiungiContatto(String utenteEmail, String contattoEmail) {
        String sql = "INSERT INTO contatto (utente_email, contatto_email) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, utenteEmail);
            pstmt.setString(2, contattoEmail);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rimuoviContatto(String utenteEmail, String contattoEmail) {
        String sql = "DELETE FROM contatto WHERE utente_email = ? AND contatto_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, utenteEmail);
            pstmt.setString(2, contattoEmail);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}