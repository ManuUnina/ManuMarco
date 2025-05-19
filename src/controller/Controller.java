package controller;

import org.ToDo.Bacheca;
import org.ToDo.Titolo;
import org.ToDo.ToDo;
import gui.View;

import java.util.EnumMap;
import java.util.Map;

    public class Controller {
        private View view;
        private final Map<Titolo, Bacheca> bacheche = new EnumMap<>(Titolo.class);

        public Controller() {
            for (Titolo t : Titolo.values()) {
                bacheche.put(t, new Bacheca(t, "Descrizione della bacheca: " + t.name()));
            }
        }

        public void init() {
            view = new View(this);
            view.setVisible(true);
        }

        public Map<Titolo, Bacheca> getBacheche() {
            return bacheche;
        }

        public void aggiungiToDo(Titolo titolo, ToDo todo) {
            Bacheca bacheca = bacheche.get(titolo);
            if (bacheca != null) {
                bacheca.aggiungiToDo(todo);
            }
        }

        public void rimuoviToDo(Titolo titolo, int index) {
            Bacheca bacheca = bacheche.get(titolo);
            if (bacheca != null) {
                bacheca.rimuoviToDo(index);
            }
        }
    }
