package dao;

import org.ToDo.ListaUtenti;

public interface ListaUtentiDAO {
    void addUserToSharedList(int todoId, String email);
    ListaUtenti getSharedUsersForToDo(int todoId, String autoreEmail);
    ListaUtenti getContattiForUser(String utenteEmail);
    void aggiungiContatto(String utenteEmail, String contattoEmail);
    void rimuoviContatto(String utenteEmail, String contattoEmail);
}