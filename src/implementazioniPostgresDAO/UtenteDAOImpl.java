package implementazioniPostgresDAO;

import dao.UtenteDAO;
import org.ToDo.Utente;
import util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UtenteDAOImpl implements UtenteDAO {

    private static final Logger LOGGER = Logger.getLogger(UtenteDAOImpl.class.getName());

    @Override
    public Utente findByEmailAndPassword(String email, String password) {
        String sql = "SELECT email, password FROM utente WHERE email = ? AND password = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Utente(rs.getString("email"), rs.getString("password"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la ricerca dell'utente con email: " + email, e);
        }
        return null;
    }

    @Override
    public boolean isEmailRegistered(String email) {
        String sql = "SELECT 1 FROM utente WHERE email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la verifica dell'email: " + email, e);
        }
        return false;
    }

    @Override
    public boolean registraNuovoUtente(Utente utente) {
        String sql = "INSERT INTO utente (email, password) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, utente.getEmail());
            pstmt.setString(2, utente.getPassword());

            int righeInserite = pstmt.executeUpdate();
            return righeInserite > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la registrazione del nuovo utente con email: " + utente.getEmail(), e);
            return false;
        }
    }
}