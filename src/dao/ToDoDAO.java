package dao;

import org.ToDo.Titolo;
import org.ToDo.ToDo;

import java.util.List;

public interface ToDoDAO {
    List<ToDo> findByBacheca(Titolo bachecaTitolo, String utenteEmail);
    void save(ToDo todo);
    void update(ToDo todo);
    void delete(int todoId);
    List<ToDo> findSharedWithUser(String userEmail);
}