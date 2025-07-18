package implementazioniPostgresDAO;

import dao.BachecaDAO;
import org.ToDo.Bacheca;
import org.ToDo.Titolo;
import util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione dell'interfaccia {@link BachecaDAO} per il database PostgreSQL.
 * Questa classe gestisce le operazioni di accesso ai dati per gli oggetti {@link Bacheca},
 * consentendo di recuperare, salvare e aggiornare le bacheche nel database.
 */
public class BachecaDAOImpl implements BachecaDAO {

    /**
     * Logger per la registrazione di errori e informazioni.
     */
    private static final Logger LOGGER = Logger.getLogger(BachecaDAOImpl.class.getName());

    /**
     * {@inheritDoc}
     * Recupera tutte le bacheche di un utente specifico dal database.
     *
     * @param utenteEmail L'email dell'utente di cui recuperare le bacheche.
     * @return una {@link Map} con i {@link Titolo} come chiave e gli oggetti {@link Bacheca} come valore.
     */
    @Override
    public Map<Titolo, Bacheca> findAllForUser(String utenteEmail) {
        Map<Titolo, Bacheca> bacheche = new EnumMap<>(Titolo.class);
        String sql = "SELECT titolo, descrizione FROM bacheca WHERE utente_email = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, utenteEmail);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Titolo titolo = Titolo.valueOf(rs.getString("titolo"));
                String descrizione = rs.getString("descrizione");
                Bacheca bacheca = new Bacheca(titolo, descrizione, utenteEmail);
                bacheche.put(titolo, bacheca);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la ricerca delle bacheche per l'utente: " + utenteEmail, e);
        }
        return bacheche;
    }

    /**
     * {@inheritDoc}
     * Salva una nuova bacheca nel database.
     */
    @Override
    public void save(Bacheca bacheca) {
        String sql = "INSERT INTO bacheca (titolo, descrizione, utente_email) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, bacheca.getTitolo(), java.sql.Types.OTHER);
            pstmt.setString(2, bacheca.getDescrizione());
            pstmt.setString(3, bacheca.getUtenteEmail());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio della bacheca", e);
        }
    }

    /**
     * {@inheritDoc}
     * Aggiorna la descrizione di una bacheca esistente.
     */
    @Override
    public void updateDescrizione(Titolo titolo, String descrizione, String utenteEmail) {
        String sql = "UPDATE bacheca SET descrizione = ? WHERE titolo = ? AND utente_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, descrizione);
            pstmt.setObject(2, titolo, java.sql.Types.OTHER);
            pstmt.setString(3, utenteEmail);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'aggiornamento della descrizione della bacheca", e);
        }
    }
}