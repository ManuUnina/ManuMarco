package gui;

import controller.Controller;
import org.ToDo.Bacheca;
import org.ToDo.Titolo;
import org.ToDo.ToDo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;

public class View extends JFrame {
    private final JComboBox<Titolo> bachecaSelector;
    private final DefaultListModel<String> todoListModel;
    private final JList<String> todoList;
    private final JButton addToDoButton;
    private final JButton removeToDoButton;
    private final JLabel descrizioneLabel;
    private final Controller controller;

    public View(Controller controller) {
        this.controller = controller;

        setTitle("Gestione Bacheche");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        bachecaSelector = new JComboBox<>(Titolo.values());
        descrizioneLabel = new JLabel("Descrizione...");

        todoListModel = new DefaultListModel<>();
        todoList = new JList<>(todoListModel);

        addToDoButton = new JButton("Aggiungi ToDo");
        removeToDoButton = new JButton("Rimuovi ToDo");

        bachecaSelector.addActionListener(e -> aggiornaListaToDo());
        addToDoButton.addActionListener(this::aggiungiToDo);
        removeToDoButton.addActionListener(this::rimuoviToDo);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(bachecaSelector, BorderLayout.NORTH);
        topPanel.add(descrizioneLabel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addToDoButton);
        bottomPanel.add(removeToDoButton);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(todoList), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        aggiornaListaToDo();
    }

    private void aggiornaListaToDo() {
        Titolo titolo = (Titolo) bachecaSelector.getSelectedItem();
        Bacheca bacheca = controller.getBacheche().get(titolo);

        descrizioneLabel.setText(bacheca.getDescrizione());

        todoListModel.clear();
        for (ToDo t : bacheca.getToDos()) {
            todoListModel.addElement(t.titolo + " - " + t.descrizione);
        }
    }

    private void aggiungiToDo(ActionEvent e) {
        JTextField titoloField = new JTextField();
        JTextField descrizioneField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Titolo:"));
        panel.add(titoloField);
        panel.add(new JLabel("Descrizione:"));
        panel.add(descrizioneField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nuovo ToDo", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Titolo selezionato = (Titolo) bachecaSelector.getSelectedItem();
            ToDo nuovo = new ToDo(
                    titoloField.getText(),
                    descrizioneField.getText(),
                    "autore", // placeholder
                    LocalDate.now().plusDays(1),
                    "posizione",
                    false,
                    "http://",
                    new java.util.ArrayList<>()
            );
            controller.aggiungiToDo(selezionato, nuovo);
            aggiornaListaToDo();
        }
    }

    private void rimuoviToDo(ActionEvent e) {
        int selected = todoList.getSelectedIndex();
        if (selected >= 0) {
            Titolo selezionato = (Titolo) bachecaSelector.getSelectedItem();
            controller.rimuoviToDo(selezionato, selected);
            aggiornaListaToDo();
        }
    }
}

