package Controller;

import org.ToDo.ListaUtenti;
import org.ToDo.Titolo;
import org.ToDo.ToDo;
import org.ToDo.Utente;

import java.util.ArrayList;

public class Controller {

    private ListaUtenti listaUtenti;
    private Utente utenteCorrente;

    public Controller() {
        this.listaUtenti = new ListaUtenti();
    }

    public void login(String username) {
        utenteCorrente = listaUtenti.getUtenteByUsername(username);
        if (utenteCorrente == null) {
            utenteCorrente = new Utente(username);
            listaUtenti.addUtente(utenteCorrente);
        }
    }

    public ArrayList<ToDo> getElencoToDoCorrente() {
        if (utenteCorrente != null) {
            return utenteCorrente.getBacheca().getElencoToDo();
        }
        return new ArrayList<>();
    }

    public void aggiungiToDo(String titolo, String descrizione) {
        if (utenteCorrente != null) {
            Titolo t = new Titolo(titolo);
            ToDo nuovo = new ToDo(t, descrizione);
            utenteCorrente.getBacheca().aggiungiToDo(nuovo);
        }
    }

    public void logout() {
        utenteCorrente = null;
    }
}

