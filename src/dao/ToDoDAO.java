package dao;

import org.ToDo.Titolo;
import org.ToDo.ToDo;
import util.ConnectionFactory;

import java.awt.Color;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ToDoDAO {

    public List<ToDo> findByBacheca(Titolo bachecaTitolo) {
        List<ToDo> toDos = new ArrayList<>();
        String sql = "SELECT * FROM todo WHERE bacheca_titolo = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, bachecaTitolo, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ToDo todo = new ToDo(
                        rs.getString("titolo"),
                        rs.getString("descrizione"),
                        rs.getDate("scadenza").toLocalDate(),
                        rs.getBoolean("stato"),
                        rs.getString("url"),
                        Color.decode(rs.getString("colore")),
                        Titolo.valueOf(rs.getString("bacheca_titolo")),
                        rs.getString("autore_email")
                );
                todo.setId(rs.getInt("id"));
                toDos.add(todo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toDos;
    }

    public void save(ToDo todo) {
        String sql = "INSERT INTO todo (titolo, descrizione, scadenza, stato, url, colore, bacheca_titolo, autore_email) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, todo.getTitolo());
            pstmt.setString(2, todo.getDescrizione());
            pstmt.setDate(3, Date.valueOf(todo.getScadenza()));
            pstmt.setBoolean(4, todo.getStato());
            pstmt.setString(5, todo.getUrl());
            pstmt.setString(6, String.format("#%06X", todo.getColore().getRGB() & 0xFFFFFF));
            pstmt.setObject(7, todo.getBachecaTitolo(), java.sql.Types.OTHER);
            pstmt.setString(8, todo.getAutoreEmail());

            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if(generatedKeys.next()){
                todo.setId(generatedKeys.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(ToDo todo){
        String sql = "UPDATE todo SET titolo = ?, descrizione = ?, scadenza = ?, stato = ?, url = ?, colore = ?, bacheca_titolo = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, todo.getTitolo());
            pstmt.setString(2, todo.getDescrizione());
            pstmt.setDate(3, Date.valueOf(todo.getScadenza()));
            pstmt.setBoolean(4, todo.getStato());
            pstmt.setString(5, todo.getUrl());
            pstmt.setString(6, String.format("#%06X", todo.getColore().getRGB() & 0xFFFFFF));
            pstmt.setObject(7, todo.getBachecaTitolo(), java.sql.Types.OTHER);
            pstmt.setInt(8, todo.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
}