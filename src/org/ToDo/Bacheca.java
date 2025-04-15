package org.ToDo;
import java.util.ArrayList;
import java.util.List;
/*
public class Bacheca {
    public Titolo titolo;
    public String descrizione;

    public Bacheca (Titolo titolo, String descrizione) {
        this.titolo = titolo;
        this.descrizione = descrizione;
    }

    public void setTitolo(Titolo titolo) {
        this.titolo = titolo;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String stampa (){
        return "Il titolo è:" + titolo + "\nla descrizione è:" + descrizione;
    }
}
*/

public class Bacheca {
    public Titolo titolo;
    private String descrizione;
    private List<ToDo> ToDos;

    public Bacheca(Titolo titolo, String descrizione) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.ToDos = new ArrayList<>();
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
        return ToDos;
    }

    public void aggiungiToDo(ToDo todo){
        ToDos.add(todo);
    }

    public void rimuoviToDo(int indice){
        if(indice >=0 && indice<ToDos.size()){
            ToDos.remove(indice);
        }
    }

    public void spostaToDo(int indice, int nuovaPosizione){
        if(indice >= 0 && indice < ToDos.size() && nuovaPosizione >=0 && nuovaPosizione < ToDos.size()){
            ToDo todo = ToDos.remove(indice);
            ToDos.add(nuovaPosizione, todo);
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(titolo + ": " + descrizione + "\n");
        for (int i = 0; i < ToDos.size(); i++) {
            sb.append(i).append(": ").append(ToDos.get(i)).append("\n");
        }
        return sb.toString();
    }
}