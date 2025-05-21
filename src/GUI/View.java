package gui;

import controller.Controller;
import org.ToDo.Bacheca;
import org.ToDo.Titolo;
import org.ToDo.ToDo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class View extends JFrame {
    private final JComboBox<Titolo> bachecaSelector;
    private final DefaultListModel<String> todoListModel;
    private final JList<String> todoList;
    private final JButton addToDoButton;
    private final JButton removeToDoButton;
    private final JButton modifyToDoButton;
    private final JButton spostaToDoButton;
    private final JLabel descrizioneLabel;
    private final JButton modificaDescrizioneBachecaButton;
    private final Controller controller;
    private final JButton logoutButton;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public View(Controller controller) {
        this.controller = controller;

        setTitle("Gestione Bacheche - Utente: " + (controller.getUtenteCorrente() != null ? controller.getUtenteCorrente().getEmail() : "N/A"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600); // Dimensioni leggermente aumentate per una migliore visualizzazione
        setLocationRelativeTo(null);

        bachecaSelector = new JComboBox<>(Titolo.values());
        descrizioneLabel = new JLabel("Descrizione...");

        todoListModel = new DefaultListModel<>();
        todoList = new JList<>(todoListModel);

        addToDoButton = new JButton("Aggiungi ToDo");
        removeToDoButton = new JButton("Rimuovi ToDo");
        modifyToDoButton = new JButton("Modifica ToDo");
        spostaToDoButton = new JButton("Sposta ToDo");
        logoutButton = new JButton("Logout");
        modificaDescrizioneBachecaButton = new JButton("Modifica Descrizione");

        bachecaSelector.addActionListener(e -> aggiornaVistaCompletaBacheca());
        addToDoButton.addActionListener(this::aggiungiToDo);
        removeToDoButton.addActionListener(this::rimuoviToDo);
        modifyToDoButton.addActionListener(this::modificaToDoSelezionato);
        spostaToDoButton.addActionListener(this::spostaToDoSelezionato);
        logoutButton.addActionListener(this::performLogout);
        modificaDescrizioneBachecaButton.addActionListener(this::modificaDescrizioneBachecaSelezionata);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(bachecaSelector, BorderLayout.NORTH);

        JPanel descriptionAreaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        descriptionAreaPanel.add(descrizioneLabel);
        descriptionAreaPanel.add(modificaDescrizioneBachecaButton);
        topPanel.add(descriptionAreaPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(addToDoButton);
        buttonPanel.add(modifyToDoButton);
        buttonPanel.add(spostaToDoButton);
        buttonPanel.add(removeToDoButton);
        buttonPanel.add(logoutButton);

        JPanel southPanelContainer = new JPanel(new BorderLayout());
        southPanelContainer.add(buttonPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(todoList), BorderLayout.CENTER);
        add(southPanelContainer, BorderLayout.SOUTH);

        aggiornaVistaCompletaBacheca();
    }

    private void aggiornaVistaCompletaBacheca() {
        Titolo titoloSelezionato = (Titolo) bachecaSelector.getSelectedItem();
        if (titoloSelezionato != null) {
            Bacheca bacheca = controller.getBacheche().get(titoloSelezionato);
            if (bacheca != null) {
                descrizioneLabel.setText(" " + bacheca.getDescrizione());
                todoListModel.clear();
                for (ToDo t : bacheca.getToDos()) {
                    String statoStr = t.getStato() ? "[X]" : "[ ]";
                    String scadenzaStr = (t.getScadenza() != null) ? t.getScadenza().format(dateFormatter) : "N/D";
                    // Formato di visualizzazione: "[X] Titolo (Scad: gg/mm/aaaa) - Descrizione"
                    todoListModel.addElement(String.format("%s %s (Scad: %s) - %s",
                            statoStr, t.getTitolo(), scadenzaStr, t.getDescrizione()));
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
        JTextField dataScadenzaField = new JTextField(10); // Per "gg/mm/aaaa"
        JCheckBox completatoCheckBox = new JCheckBox("Completato");

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Titolo:"));
        panel.add(titoloField);
        panel.add(new JLabel("Descrizione:"));
        panel.add(descrizioneField);
        panel.add(new JLabel("Data Scadenza (gg/mm/aaaa):"));
        panel.add(dataScadenzaField);
        panel.add(completatoCheckBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nuovo ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Titolo selezionato = (Titolo) bachecaSelector.getSelectedItem();
            if (selezionato != null && controller.getUtenteCorrente() != null) {
                String dataInput = dataScadenzaField.getText().trim();
                LocalDate scadenza = LocalDate.now().plusDays(7); // Predefinito a una settimana da oggi
                // boolean dataValida = false; // Non usata direttamente, ma utile per logica più complessa
                if (!dataInput.isEmpty()) {
                    try {
                        scadenza = LocalDate.parse(dataInput, dateFormatter);
                        // dataValida = true;
                    } catch (DateTimeParseException ex) {
                        JOptionPane.showMessageDialog(this, "Formato data non valido. Usare gg/mm/aaaa.\nVerrà usata una data predefinita.", "Errore Data", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Data di scadenza non inserita. Verrà usata una data predefinita.", "Info Data", JOptionPane.INFORMATION_MESSAGE);
                }

                boolean stato = completatoCheckBox.isSelected();

                ToDo nuovo = new ToDo(
                        titoloField.getText(),
                        descrizioneField.getText(),
                        controller.getUtenteCorrente().getEmail(),
                        scadenza,
                        "N/A", // placeholder per posizione
                        stato,
                        "http://example.com", // placeholder per url
                        new ArrayList<>()
                );
                controller.aggiungiToDo(selezionato, nuovo);
                aggiornaVistaCompletaBacheca();
            } else {
                JOptionPane.showMessageDialog(this, "Errore: Seleziona una bacheca e assicurati di essere loggato.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void modificaToDoSelezionato(ActionEvent e) {
        int selectedIndex = todoList.getSelectedIndex();
        Titolo bachecaSelezionata = (Titolo) bachecaSelector.getSelectedItem();

        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona un ToDo da modificare.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (bachecaSelezionata == null) { /* ... gestione errore ... */ return; }

        ToDo toDoDaModificare = controller.getToDoFromBacheca(bachecaSelezionata, selectedIndex);
        if (toDoDaModificare == null) { /* ... gestione errore ... */ return; }

        JTextField titoloField = new JTextField(toDoDaModificare.getTitolo());
        JTextField descrizioneField = new JTextField(toDoDaModificare.getDescrizione());
        JTextField dataScadenzaField = new JTextField(toDoDaModificare.getScadenza() != null ? toDoDaModificare.getScadenza().format(dateFormatter) : "");
        JCheckBox completatoCheckBox = new JCheckBox("Completato", toDoDaModificare.getStato());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Titolo:"));
        panel.add(titoloField);
        panel.add(new JLabel("Descrizione:"));
        panel.add(descrizioneField);
        panel.add(new JLabel("Data Scadenza (gg/mm/aaaa):"));
        panel.add(dataScadenzaField);
        panel.add(completatoCheckBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Modifica ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String nuovoTitolo = titoloField.getText();
            String nuovaDescrizione = descrizioneField.getText();
            String dataInput = dataScadenzaField.getText().trim();
            boolean nuovoStato = completatoCheckBox.isSelected();

            LocalDate nuovaScadenza = toDoDaModificare.getScadenza(); // Mantiene la vecchia data se la nuova non è valida
            if (!dataInput.isEmpty()) {
                try {
                    nuovaScadenza = LocalDate.parse(dataInput, dateFormatter);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Formato data non valido. Usare gg/mm/aaaa.\nLa data di scadenza non sarà modificata.", "Errore Data", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Data di scadenza rimossa o non valida. La data originale verrà mantenuta se possibile, o impostata come predefinita.", "Info Data", JOptionPane.INFORMATION_MESSAGE);
                if (nuovaScadenza == null) nuovaScadenza = LocalDate.now().plusDays(7); // Assicura un valore predefinito
            }

            controller.modificaToDo(bachecaSelezionata, selectedIndex, nuovoTitolo, nuovaDescrizione, nuovaScadenza, nuovoStato);
            aggiornaVistaCompletaBacheca();
        }
    }

    private void spostaToDoSelezionato(ActionEvent e) {
        // ... (codice invariato)
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

    private void modificaDescrizioneBachecaSelezionata(ActionEvent e) {
        // ... (codice invariato)
        Titolo bachecaSelezionata = (Titolo) bachecaSelector.getSelectedItem();

        if (bachecaSelezionata == null) {
            JOptionPane.showMessageDialog(this, "Seleziona una bacheca per modificarne la descrizione.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Bacheca bachecaCorrente = controller.getBacheche().get(bachecaSelezionata);
        if (bachecaCorrente == null) {
            JOptionPane.showMessageDialog(this, "Errore: Bacheca non trovata.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String descrizioneAttuale = bachecaCorrente.getDescrizione();

        String nuovaDescrizioneIngresso = JOptionPane.showInputDialog( // Rinominato per chiarezza
                this,
                "Modifica descrizione per la bacheca '" + bachecaSelezionata.name() + "':",
                "Modifica Descrizione Bacheca",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                descrizioneAttuale
        ).toString();

        // JOptionPane.showInputDialog restituisce null se l'utente preme Annulla o chiude la finestra
        if (nuovaDescrizioneIngresso != null) {
            controller.modificaDescrizioneBacheca(bachecaSelezionata, nuovaDescrizioneIngresso);
            aggiornaVistaCompletaBacheca();
        }
    }

    private void performLogout(ActionEvent e) {
        // ... (codice invariato)
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