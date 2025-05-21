package gui;

import controller.Controller;
import org.ToDo.Bacheca; //
import org.ToDo.Titolo; //
import org.ToDo.ToDo; //

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate; //
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class View extends JFrame {
    private final JComboBox<Titolo> bachecaSelector;
    private final DefaultListModel<String> todoListModel;
    private final JList<String> todoList;
    private final JButton addToDoButton;
    private final JButton modifyToDoButton;
    private final JButton spostaToDoButton; // Bottone per spostare ToDo
    private final JButton removeToDoButton;
    private final JLabel descrizioneLabel;
    private final Controller controller;
    private final JButton logoutButton;

    public View(Controller controller) {
        this.controller = controller;

        setTitle("Gestione Bacheche - Utente: " + (controller.getUtenteCorrente() != null ? controller.getUtenteCorrente().getEmail() : "N/A")); //
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 550); // Aumentata leggermente la dimensione per i nuovi bottoni
        setLocationRelativeTo(null);

        bachecaSelector = new JComboBox<>(Titolo.values()); //
        descrizioneLabel = new JLabel("Descrizione..."); //

        todoListModel = new DefaultListModel<>(); //
        todoList = new JList<>(todoListModel); //

        addToDoButton = new JButton("Aggiungi ToDo"); //
        removeToDoButton = new JButton("Rimuovi ToDo"); //
        modifyToDoButton = new JButton("Modifica ToDo"); //
        spostaToDoButton = new JButton("Sposta ToDo"); // Istanziazione del bottone Sposta
        logoutButton = new JButton("Logout"); //

        bachecaSelector.addActionListener(e -> aggiornaListaToDo());
        addToDoButton.addActionListener(this::aggiungiToDo);
        removeToDoButton.addActionListener(this::rimuoviToDo);
        modifyToDoButton.addActionListener(this::modificaToDoSelezionato);
        spostaToDoButton.addActionListener(this::spostaToDoSelezionato); // Azione per spostare ToDo
        logoutButton.addActionListener(this::performLogout); //

        JPanel topPanel = new JPanel(new BorderLayout()); //
        topPanel.add(bachecaSelector, BorderLayout.NORTH); //
        topPanel.add(descrizioneLabel, BorderLayout.CENTER); //

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); //
        buttonPanel.add(addToDoButton); //
        buttonPanel.add(modifyToDoButton); //
        buttonPanel.add(spostaToDoButton); // Aggiunta del bottone Sposta al pannello
        buttonPanel.add(removeToDoButton); //
        buttonPanel.add(logoutButton); //

        JPanel southPanelContainer = new JPanel(new BorderLayout()); //
        southPanelContainer.add(buttonPanel, BorderLayout.CENTER); //

        setLayout(new BorderLayout()); //
        add(topPanel, BorderLayout.NORTH); //
        add(new JScrollPane(todoList), BorderLayout.CENTER); //
        add(southPanelContainer, BorderLayout.SOUTH); //

        aggiornaListaToDo();
    }

    private void aggiornaListaToDo() {
        Titolo titoloSelezionato = (Titolo) bachecaSelector.getSelectedItem(); //
        if (titoloSelezionato != null) {
            Bacheca bacheca = controller.getBacheche().get(titoloSelezionato); //
            if (bacheca != null) {
                descrizioneLabel.setText(bacheca.getDescrizione()); //
                todoListModel.clear(); //
                for (ToDo t : bacheca.getToDos()) { //
                    todoListModel.addElement(t.getTitolo() + " - " + t.getDescrizione()); //
                }
            } else {
                descrizioneLabel.setText("Bacheca non trovata."); //
                todoListModel.clear(); //
            }
        } else {
            descrizioneLabel.setText("Nessuna bacheca selezionata."); //
            todoListModel.clear(); //
        }
    }

    private void aggiungiToDo(ActionEvent e) {
        JTextField titoloField = new JTextField(); //
        JTextField descrizioneField = new JTextField(); //

        JPanel panel = new JPanel(new GridLayout(0, 1)); //
        panel.add(new JLabel("Titolo:")); //
        panel.add(titoloField); //
        panel.add(new JLabel("Descrizione:")); //
        panel.add(descrizioneField); //

        int result = JOptionPane.showConfirmDialog(this, panel, "Nuovo ToDo", JOptionPane.OK_CANCEL_OPTION); //
        if (result == JOptionPane.OK_OPTION) { //
            Titolo selezionato = (Titolo) bachecaSelector.getSelectedItem(); //
            if (selezionato != null && controller.getUtenteCorrente() != null) {
                ToDo nuovo = new ToDo(
                        titoloField.getText(), //
                        descrizioneField.getText(), //
                        controller.getUtenteCorrente().getEmail(), //
                        LocalDate.now().plusDays(1),
                        "N/A",
                        false,
                        "http://example.com",
                        new ArrayList<>()
                );
                controller.aggiungiToDo(selezionato, nuovo); //
                aggiornaListaToDo(); //
            } else {
                JOptionPane.showMessageDialog(this, "Errore: Seleziona una bacheca e assicurati di essere loggato.", "Errore", JOptionPane.ERROR_MESSAGE); //
            }
        }
    }

    private void rimuoviToDo(ActionEvent e) {
        int selectedIndex = todoList.getSelectedIndex(); //
        if (selectedIndex >= 0) { //
            Titolo selezionato = (Titolo) bachecaSelector.getSelectedItem(); //
            if (selezionato != null) { //
                controller.rimuoviToDo(selezionato, selectedIndex); //
                aggiornaListaToDo(); //
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleziona un ToDo da rimuovere.", "Attenzione", JOptionPane.WARNING_MESSAGE); //
        }
    }

    private void modificaToDoSelezionato(ActionEvent e) {
        int selectedIndex = todoList.getSelectedIndex(); //
        Titolo bachecaSelezionata = (Titolo) bachecaSelector.getSelectedItem(); //

        if (selectedIndex < 0) { //
            JOptionPane.showMessageDialog(this, "Seleziona un ToDo da modificare.", "Attenzione", JOptionPane.WARNING_MESSAGE); //
            return;
        }

        if (bachecaSelezionata == null) { //
            JOptionPane.showMessageDialog(this, "Seleziona una bacheca.", "Errore", JOptionPane.ERROR_MESSAGE); //
            return;
        }

        ToDo toDoDaModificare = controller.getToDoFromBacheca(bachecaSelezionata, selectedIndex); //
        if (toDoDaModificare == null) { //
            JOptionPane.showMessageDialog(this, "Errore nel recuperare il ToDo.", "Errore", JOptionPane.ERROR_MESSAGE); //
            return;
        }

        JTextField titoloField = new JTextField(toDoDaModificare.getTitolo()); //
        JTextField descrizioneField = new JTextField(toDoDaModificare.getDescrizione()); //

        JPanel panel = new JPanel(new GridLayout(0, 1)); //
        panel.add(new JLabel("Nuovo Titolo:")); //
        panel.add(titoloField); //
        panel.add(new JLabel("Nuova Descrizione:")); //
        panel.add(descrizioneField); //

        int result = JOptionPane.showConfirmDialog(this, panel, "Modifica ToDo", JOptionPane.OK_CANCEL_OPTION); //
        if (result == JOptionPane.OK_OPTION) { //
            String nuovoTitolo = titoloField.getText(); //
            String nuovaDescrizione = descrizioneField.getText(); //

            controller.modificaToDo(bachecaSelezionata, selectedIndex, nuovoTitolo, nuovaDescrizione); //
            aggiornaListaToDo(); //
        }
    }

    private void spostaToDoSelezionato(ActionEvent e) {
        int selectedIndex = todoList.getSelectedIndex();
        Titolo bachecaOrigine = (Titolo) bachecaSelector.getSelectedItem();

        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona un ToDo da spostare.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (bachecaOrigine == null) {
            // Questo non dovrebbe accadere se un ToDo è selezionato, ma per sicurezza
            JOptionPane.showMessageDialog(this, "Bacheca di origine non valida.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Prepara la lista delle bacheche di destinazione (tutte tranne quella di origine)
        Titolo[] tutteLeBacheche = Titolo.values(); //
        ArrayList<Titolo> opzioniDestinazioneList = new ArrayList<>(Arrays.asList(tutteLeBacheche));
        opzioniDestinazioneList.remove(bachecaOrigine);

        if (opzioniDestinazioneList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Non ci sono altre bacheche disponibili per lo spostamento.", "Informazione", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Titolo[] opzioniDestinazioneArray = opzioniDestinazioneList.toArray(new Titolo[0]);

        JComboBox<Titolo> destinazioneSelector = new JComboBox<>(opzioniDestinazioneArray);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Sposta in bacheca:"));
        panel.add(destinazioneSelector);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sposta ToDo", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Titolo bachecaDestinazione = (Titolo) destinazioneSelector.getSelectedItem();
            if (bachecaDestinazione != null) {
                controller.spostaToDoGUI(bachecaOrigine, selectedIndex, bachecaDestinazione);
                aggiornaListaToDo(); // Aggiorna la vista della bacheca corrente
            }
        }
    }

    private void performLogout(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler effettuare il logout?",
                "Conferma Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE); //

        if (confirm == JOptionPane.YES_OPTION) { //
            controller.eseguiLogout();
        }
    }
}