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

public class BachecaDAOImpl implements BachecaDAO {

    private static final Logger LOGGER = Logger.getLogger(BachecaDAOImpl.class.getName());

    @Override
    public Map<Titolo, Bacheca> findAllForUser(String utenteEmail) {
        Map<Titolo, Bacheca> bacheche = new EnumMap<>(Titolo.class);
        // La query ora seleziona solo le colonne necessarie
        String sql = "SELECT titolo, descrizione FROM bacheca WHERE utente_email = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, utenteEmail);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Titolo titolo = Titolo.valueOf(rs.getString("titolo"));
                String descrizione = rs.getString("descrizione");
                // L'utenteEmail viene passato come parametro, quindi non è necessario selezionarlo
                Bacheca bacheca = new Bacheca(titolo, descrizione, utenteEmail);
                bacheche.put(titolo, bacheca);
            }

        } catch (SQLException e) {
            // Sostituito e.printStackTrace() con un logger
            LOGGER.log(Level.SEVERE, "Errore durante la ricerca delle bacheche per l'utente: " + utenteEmail, e);
        }
        return bacheche;
    }

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