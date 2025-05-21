package gui;

import controller.Controller;
import org.ToDo.Bacheca;
import org.ToDo.Titolo;
import org.ToDo.ToDo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
// import java.util.stream.Collectors; // Non più usato direttamente qui

public class View extends JFrame {
    private final JComboBox<Titolo> bachecaSelector;
    private final DefaultListModel<String> todoListModel;
    private final JList<String> todoList;
    private final JButton addToDoButton;
    private final JButton removeToDoButton;
    private final JButton modifyToDoButton;
    private final JButton spostaToDoButton;
    private final JLabel descrizioneLabel;
    private final JButton modificaDescrizioneBachecaButton; // Nuovo bottone
    private final Controller controller;
    private final JButton logoutButton;

    public View(Controller controller) {
        this.controller = controller;

        setTitle("Gestione Bacheche - Utente: " + (controller.getUtenteCorrente() != null ? controller.getUtenteCorrente().getEmail() : "N/A"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 550);
        setLocationRelativeTo(null);

        bachecaSelector = new JComboBox<>(Titolo.values());
        descrizioneLabel = new JLabel("Descrizione..."); // Sarà aggiornata dinamicamente

        todoListModel = new DefaultListModel<>();
        todoList = new JList<>(todoListModel);

        addToDoButton = new JButton("Aggiungi ToDo");
        removeToDoButton = new JButton("Rimuovi ToDo");
        modifyToDoButton = new JButton("Modifica ToDo");
        spostaToDoButton = new JButton("Sposta ToDo");
        logoutButton = new JButton("Logout");
        modificaDescrizioneBachecaButton = new JButton("Modifica Descrizione"); // Istanziazione

        bachecaSelector.addActionListener(e -> aggiornaVistaCompletaBacheca()); // Rinominato per chiarezza
        addToDoButton.addActionListener(this::aggiungiToDo);
        removeToDoButton.addActionListener(this::rimuoviToDo);
        modifyToDoButton.addActionListener(this::modificaToDoSelezionato);
        spostaToDoButton.addActionListener(this::spostaToDoSelezionato);
        logoutButton.addActionListener(this::performLogout);
        modificaDescrizioneBachecaButton.addActionListener(this::modificaDescrizioneBachecaSelezionata); // Azione per il nuovo bottone

        // Pannello superiore per selettore bacheca e area descrizione
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(bachecaSelector, BorderLayout.NORTH);

        JPanel descriptionAreaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Pannello per etichetta descrizione e bottone modifica
        descriptionAreaPanel.add(descrizioneLabel);
        descriptionAreaPanel.add(modificaDescrizioneBachecaButton);
        topPanel.add(descriptionAreaPanel, BorderLayout.CENTER);


        // Pannello per i bottoni ToDo e Logout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(addToDoButton);
        buttonPanel.add(modifyToDoButton);
        buttonPanel.add(spostaToDoButton);
        buttonPanel.add(removeToDoButton);
        buttonPanel.add(logoutButton);

        JPanel southPanelContainer = new JPanel(new BorderLayout());
        southPanelContainer.add(buttonPanel, BorderLayout.CENTER);


        // Layout principale della finestra
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(todoList), BorderLayout.CENTER);
        add(southPanelContainer, BorderLayout.SOUTH);

        aggiornaVistaCompletaBacheca(); // Chiamata iniziale
    }

    // Metodo rinominato per indicare che aggiorna sia la lista ToDo che la descrizione
    private void aggiornaVistaCompletaBacheca() {
        Titolo titoloSelezionato = (Titolo) bachecaSelector.getSelectedItem();
        if (titoloSelezionato != null) {
            Bacheca bacheca = controller.getBacheche().get(titoloSelezionato);
            if (bacheca != null) {
                descrizioneLabel.setText(" " + bacheca.getDescrizione()); // Aggiorna etichetta descrizione
                todoListModel.clear();
                for (ToDo t : bacheca.getToDos()) {
                    todoListModel.addElement(t.getTitolo() + " - " + t.getDescrizione());
                }
            } else {
                descrizioneLabel.setText("Bacheca non trovata.");
                todoListModel.clear();
            }
        } else {
            descrizioneLabel.setText("Nessuna bacheca selezionata.");
            todoListModel.clear();
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
            if (selezionato != null && controller.getUtenteCorrente() != null) {
                ToDo nuovo = new ToDo(
                        titoloField.getText(),
                        descrizioneField.getText(),
                        controller.getUtenteCorrente().getEmail(),
                        LocalDate.now().plusDays(1),
                        "N/A",
                        false,
                        "http://example.com",
                        new ArrayList<>()
                );
                controller.aggiungiToDo(selezionato, nuovo);
                aggiornaVistaCompletaBacheca();
            } else {
                JOptionPane.showMessageDialog(this, "Errore: Seleziona una bacheca e assicurati di essere loggato.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void rimuoviToDo(ActionEvent e) {
        int selectedIndex = todoList.getSelectedIndex();
        if (selectedIndex >= 0) {
            Titolo selezionato = (Titolo) bachecaSelector.getSelectedItem();
            if (selezionato != null) {
                controller.rimuoviToDo(selezionato, selectedIndex);
                aggiornaVistaCompletaBacheca();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleziona un ToDo da rimuovere.", "Attenzione", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void modificaToDoSelezionato(ActionEvent e) {
        int selectedIndex = todoList.getSelectedIndex();
        Titolo bachecaSelezionata = (Titolo) bachecaSelector.getSelectedItem();

        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona un ToDo da modificare.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (bachecaSelezionata == null) {
            JOptionPane.showMessageDialog(this, "Seleziona una bacheca.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ToDo toDoDaModificare = controller.getToDoFromBacheca(bachecaSelezionata, selectedIndex);
        if (toDoDaModificare == null) {
            JOptionPane.showMessageDialog(this, "Errore nel recuperare il ToDo.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField titoloField = new JTextField(toDoDaModificare.getTitolo());
        JTextField descrizioneField = new JTextField(toDoDaModificare.getDescrizione());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nuovo Titolo:"));
        panel.add(titoloField);
        panel.add(new JLabel("Nuova Descrizione:"));
        panel.add(descrizioneField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Modifica ToDo", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String nuovoTitolo = titoloField.getText();
            String nuovaDescrizione = descrizioneField.getText();

            controller.modificaToDo(bachecaSelezionata, selectedIndex, nuovoTitolo, nuovaDescrizione);
            aggiornaVistaCompletaBacheca();
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
            JOptionPane.showMessageDialog(this, "Bacheca di origine non valida.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Titolo[] tutteLeBacheche = Titolo.values();
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
                aggiornaVistaCompletaBacheca();
            }
        }
    }

    // Nuovo metodo per gestire la modifica della descrizione della bacheca
    private void modificaDescrizioneBachecaSelezionata(ActionEvent e) {
        Titolo bachecaSelezionata = (Titolo) bachecaSelector.getSelectedItem();

        if (bachecaSelezionata == null) {
            JOptionPane.showMessageDialog(this, "Seleziona una bacheca per modificarne la descrizione.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Bacheca bachecaCorrente = controller.getBacheche().get(bachecaSelezionata);
        if (bachecaCorrente == null) { // Controllo di sicurezza, non dovrebbe accadere
            JOptionPane.showMessageDialog(this, "Errore: Bacheca non trovata.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String descrizioneAttuale = bachecaCorrente.getDescrizione();

        String nuovaDescrizione = JOptionPane.showInputDialog(
                this,
                "Modifica descrizione per la bacheca '" + bachecaSelezionata.name() + "':",
                "Modifica Descrizione Bacheca",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                descrizioneAttuale
        ).toString();

        // L'utente ha premuto OK e il testo potrebbe essere vuoto o diverso
        if (nuovaDescrizione != null) {
            controller.modificaDescrizioneBacheca(bachecaSelezionata, nuovaDescrizione);
            aggiornaVistaCompletaBacheca(); // Aggiorna la label della descrizione e il resto se necessario
        }
        // Se l'utente preme Annulla, nuovaDescrizione sarà null e non si fa nulla
    }

    private void performLogout(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler effettuare il logout?",
                "Conferma Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.eseguiLogout();
        }
    }
}