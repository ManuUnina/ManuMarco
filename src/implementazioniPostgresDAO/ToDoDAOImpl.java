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

public class ToDoDAOImpl implements ToDoDAO {

    private final ListaUtentiDAO listaUtentiDAO = new ListaUtentiDAOImpl();

    @Override
    public List<ToDo> findByBacheca(Titolo bachecaTitolo, String utenteEmail) {
        List<ToDo> toDos = new ArrayList<>();
        String sql = "SELECT * FROM todo WHERE bacheca_titolo = ? AND autore_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, bachecaTitolo, java.sql.Types.OTHER);
            pstmt.setString(2, utenteEmail);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
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
                toDos.add(todo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toDos;
    }

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
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int todoId){
        String sql = "DELETE FROM todo WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, todoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ToDo> findSharedWithUser(String userEmail) {
        List<ToDo> toDos = new ArrayList<>();
        String sql = "SELECT t.* FROM todo t JOIN todo_utenti_condivisi s ON t.id = s.todo_id WHERE s.utente_email = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
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
                toDos.add(todo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toDos;
    }
}