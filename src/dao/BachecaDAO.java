package dao;

import org.ToDo.Bacheca;
import org.ToDo.Titolo;
import util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

public class BachecaDAO {

    public Map<Titolo, Bacheca> findAllForUser(String utenteEmail) {
        Map<Titolo, Bacheca> bacheche = new EnumMap<>(Titolo.class);
        String sql = "SELECT * FROM bacheca WHERE utente_email = ?";

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
            e.printStackTrace();
        }
        return bacheche;
    }

    public void save(Bacheca bacheca) {
        String sql = "INSERT INTO bacheca (titolo, descrizione, utente_email) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, bacheca.getTitolo(), java.sql.Types.OTHER);
            pstmt.setString(2, bacheca.getDescrizione());
            pstmt.setString(3, bacheca.getUtenteEmail());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateDescrizione(Titolo titolo, String descrizione, String utenteEmail) {
        String sql = "UPDATE bacheca SET descrizione = ? WHERE titolo = ? AND utente_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, descrizione);
            pstmt.setObject(2, titolo, java.sql.Types.OTHER);
            pstmt.setString(3, utenteEmail);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
