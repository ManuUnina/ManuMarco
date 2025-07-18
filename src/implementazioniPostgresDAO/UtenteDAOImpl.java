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

/**
 * Implementazione dell'interfaccia {@link UtenteDAO} per il database PostgreSQL.
 * Questa classe si occupa della gestione della persistenza dei dati degli utenti,
 * includendo operazioni di ricerca, verifica e registrazione.
 */
public class UtenteDAOImpl implements UtenteDAO {

    /**
     * Logger per la registrazione di errori e informazioni.
     */
    private static final Logger LOGGER = Logger.getLogger(UtenteDAOImpl.class.getName());

    /**
     * {@inheritDoc}
     * Cerca un utente nel database utilizzando email and password. Questo metodo è tipicamente
     * utilizzato per il processo di autenticazione (login).
     *
     * @param email L'email dell'utente da cercare.
     * @param password La password dell'utente.
     * @return un oggetto {@link Utente} se le credenziali corrispondono a un record nel database,
     * altrimenti {@code null}.
     */
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

    /**
     * {@inheritDoc}
     * Verifica se un'email è già presente nella tabella degli utenti.
     * Utile per prevenire registrazioni multiple con la stessa email.
     *
     * @param email L'email da verificare.
     * @return {@code true} se l'email è già registrata, {@code false} altrimenti.
     */
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

    /**
     * {@inheritDoc}
     * Inserisce un nuovo utente nella tabella 'utente'.
     *
     * @param utente L'oggetto {@link Utente} da registrare.
     * @return {@code true} se l'inserimento ha successo (una o più righe inserite),
     * {@code false} in caso di errore o se nessuna riga viene inserita.
     */
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