package org.ToDo;

import java.util.ArrayList;
import java.util.List;

public class Bacheca {
    private Titolo titolo;
    private String descrizione;
    private String utenteEmail;
    private List<ToDo> toDos;

    public Bacheca(Titolo titolo, String descrizione, String utenteEmail) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.utenteEmail = utenteEmail;
        this.toDos = new ArrayList<>();
    }

    // --- METODO MANCANTE AGGIUNTO QUI ---
    public String getUtenteEmail() {
        return utenteEmail;
    }
    // ------------------------------------

    public Titolo getTitolo() {
        return titolo;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public List<ToDo> getToDos() {
        return toDos;
    }

    public void setToDos(List<ToDo> toDos) {
        this.toDos = toDos;
    }

    public void aggiungiToDo(ToDo todo) {
        toDos.add(todo);
    }

    public void rimuoviToDo(int indice) {
        if (indice >= 0 && indice < toDos.size()) {
            toDos.remove(indice);
        }
    }

    @Override
    public String toString() {
        // CORREZIONE: Gestisce il caso in cui il titolo è null (per la bacheca condivisa)
        if (titolo == null) {
            return "Bacheca Condivisa";
        }
        return titolo.name();
    }
}