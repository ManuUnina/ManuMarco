package dao;

import org.ToDo.Bacheca;
import org.ToDo.Titolo;

import java.util.Map;

public interface BachecaDAO {
    Map<Titolo, Bacheca> findAllForUser(String utenteEmail);
    void save(Bacheca bacheca);
    void updateDescrizione(Titolo titolo, String descrizione, String utenteEmail);
}