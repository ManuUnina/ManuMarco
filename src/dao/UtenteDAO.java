package dao;

import org.ToDo.Utente;

public interface UtenteDAO {
    Utente findByEmailAndPassword(String email, String password);
    boolean isEmailRegistered(String email);
    boolean registraNuovoUtente(Utente utente);
}