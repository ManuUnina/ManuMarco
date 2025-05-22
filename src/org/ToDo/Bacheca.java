package org.ToDo;

import java.util.ArrayList;
import java.util.List;

public class Bacheca {
    public Titolo titolo;
    private String descrizione;
    private List<ToDo> toDos; // Rinominato da ToDos a toDos per convenzione

    public Bacheca(Titolo titolo, String descrizione) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.toDos = new ArrayList<>(); // Usa il campo rinominato
    }

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
        return toDos; // Usa il campo rinominato
    }

    public void aggiungiToDo(ToDo todo) {
        toDos.add(todo); // Usa il campo rinominato
    }

    public void rimuoviToDo(int indice) {
        if (indice >= 0 && indice < toDos.size()) { // Usa il campo rinominato
            toDos.remove(indice); // Usa il campo rinominato
        }
    }

    public void spostaToDo(int indice, int nuovaPosizione) {
        if (indice >= 0 && indice < toDos.size() && nuovaPosizione >= 0 && nuovaPosizione < toDos.size()) { // Usa il campo rinominato
            ToDo todo = toDos.remove(indice); // Usa il campo rinominato
            toDos.add(nuovaPosizione, todo); // Usa il campo rinominato
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(titolo + ": " + descrizione + "\n");
        for (int i = 0; i < toDos.size(); i++) { // Usa il campo rinominato
            sb.append(i).append(": ").append(toDos.get(i)).append("\n"); // Usa il campo rinominato
        }
        return sb.toString();
    }

    // RIMUOVI TUTTI I SEGUENTI METODI STATICI:
    // public static void inizializzaBacheche() { ... }
    // static void visualizzaBacheche() { ... }
    // public static Titolo scegliBacheca() { ... }
    // public static void visualizzaBacheca(Titolo titolo) { ... }
}