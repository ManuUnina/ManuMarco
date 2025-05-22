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
    // Modifica: DefaultListModel e JList ora usano ToDo
    private final DefaultListModel<ToDo> todoListModel;
    private final JList<ToDo> todoList;
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
        setSize(800, 600);
        setLocationRelativeTo(null);

        bachecaSelector = new JComboBox<>(Titolo.values());
        descrizioneLabel = new JLabel("Descrizione...");

        // Modifica: Inizializzazione per ToDo
        todoListModel = new DefaultListModel<>();
        todoList = new JList<>(todoListModel);
        todoList.setCellRenderer(new ToDoCellRenderer()); // Imposta il renderer personalizzato

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
                    // Modifica: Aggiunge l'oggetto ToDo direttamente al modello
                    todoListModel.addElement(t);
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
        JTextField dataScadenzaField = new JTextField(10);
        JCheckBox completatoCheckBox = new JCheckBox("Completato");

        // Componenti per la selezione del colore
        final Color[] selectedColor = {Color.WHITE}; // Colore predefinito
        JButton colorButton = new JButton("Scegli Colore");
        JLabel colorPreview = new JLabel("  ");
        colorPreview.setOpaque(true);
        colorPreview.setBackground(selectedColor[0]);
        colorPreview.setPreferredSize(new Dimension(20, 20));
        colorButton.addActionListener(ev -> {
            Color chosenColor = JColorChooser.showDialog(this, "Scegli un colore per il ToDo", selectedColor[0]);
            if (chosenColor != null) {
                selectedColor[0] = chosenColor;
                colorPreview.setBackground(chosenColor);
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Titolo:"));
        panel.add(titoloField);
        panel.add(new JLabel("Descrizione:"));
        panel.add(descrizioneField);
        panel.add(new JLabel("Data Scadenza (gg/mm/aaaa):"));
        panel.add(dataScadenzaField);
        panel.add(completatoCheckBox);

        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanel.add(colorButton);
        colorPanel.add(new JLabel(" Colore:"));
        colorPanel.add(colorPreview);
        panel.add(colorPanel);


        int result = JOptionPane.showConfirmDialog(this, panel, "Nuovo ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Titolo selezionato = (Titolo) bachecaSelector.getSelectedItem();
            if (selezionato != null && controller.getUtenteCorrente() != null) {
                String dataInput = dataScadenzaField.getText().trim();
                LocalDate scadenza = LocalDate.now().plusDays(7);
                if (!dataInput.isEmpty()) {
                    try {
                        scadenza = LocalDate.parse(dataInput, dateFormatter);
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
                        "N/A",
                        stato,
                        "http://example.com",
                        new ArrayList<>(),
                        selectedColor[0] // Passa il colore selezionato
                );
                controller.aggiungiToDo(selezionato, nuovo);
                aggiornaVistaCompletaBacheca();
            } else {
                JOptionPane.showMessageDialog(this, "Errore: Seleziona una bacheca e assicurati di essere loggato.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void modificaToDoSelezionato(ActionEvent e) {
        // Modifica: usa getSelectedValue() dato che la lista ora contiene ToDo
        ToDo toDoDaModificare = todoList.getSelectedValue();
        int selectedIndex = todoList.getSelectedIndex(); // L'indice è ancora necessario per il controller
        Titolo bachecaSelezionata = (Titolo) bachecaSelector.getSelectedItem();

        if (toDoDaModificare == null) { // Se nessun ToDo è selezionato
            JOptionPane.showMessageDialog(this, "Seleziona un ToDo da modificare.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Non è più necessario chiamare controller.getToDoFromBacheca se JList<ToDo> è usato correttamente.
        // ToDo toDoDaModificare = controller.getToDoFromBacheca(bachecaSelezionata, selectedIndex);

        if (bachecaSelezionata == null) {
            JOptionPane.showMessageDialog(this, "Nessuna bacheca selezionata.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField titoloField = new JTextField(toDoDaModificare.getTitolo());
        JTextField descrizioneField = new JTextField(toDoDaModificare.getDescrizione());
        JTextField dataScadenzaField = new JTextField(toDoDaModificare.getScadenza() != null ? toDoDaModificare.getScadenza().format(dateFormatter) : "");
        JCheckBox completatoCheckBox = new JCheckBox("Completato", toDoDaModificare.getStato());

        // Componenti per la modifica del colore
        final Color[] currentSelectedColor = {toDoDaModificare.getColore() != null ? toDoDaModificare.getColore() : Color.WHITE};
        JButton colorButtonModify = new JButton("Scegli Colore");
        JLabel colorPreviewModify = new JLabel("  ");
        colorPreviewModify.setOpaque(true);
        colorPreviewModify.setBackground(currentSelectedColor[0]);
        colorPreviewModify.setPreferredSize(new Dimension(20, 20));
        colorButtonModify.addActionListener(ev -> {
            Color chosenColor = JColorChooser.showDialog(this, "Modifica colore del ToDo", currentSelectedColor[0]);
            if (chosenColor != null) {
                currentSelectedColor[0] = chosenColor;
                colorPreviewModify.setBackground(chosenColor);
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Titolo:"));
        panel.add(titoloField);
        panel.add(new JLabel("Descrizione:"));
        panel.add(descrizioneField);
        panel.add(new JLabel("Data Scadenza (gg/mm/aaaa):"));
        panel.add(dataScadenzaField);
        panel.add(completatoCheckBox);

        JPanel colorPanelModify = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanelModify.add(colorButtonModify);
        colorPanelModify.add(new JLabel(" Colore:"));
        colorPanelModify.add(colorPreviewModify);
        panel.add(colorPanelModify);


        int result = JOptionPane.showConfirmDialog(this, panel, "Modifica ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String nuovoTitolo = titoloField.getText();
            String nuovaDescrizione = descrizioneField.getText();
            String dataInput = dataScadenzaField.getText().trim();
            boolean nuovoStato = completatoCheckBox.isSelected();

            LocalDate nuovaScadenza = toDoDaModificare.getScadenza();
            if (!dataInput.isEmpty()) {
                try {
                    nuovaScadenza = LocalDate.parse(dataInput, dateFormatter);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Formato data non valido. Usare gg/mm/aaaa.\nLa data di scadenza non sarà modificata.", "Errore Data", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Se il campo data è vuoto, si potrebbe decidere di rimuovere la scadenza (impostarla a null)
                // o mantenere quella esistente. Qui manteniamo quella esistente o una predefinita.
                if (nuovaScadenza == null && toDoDaModificare.getScadenza() == null) { // se non c'era e non viene inserita
                    JOptionPane.showMessageDialog(this, "Data di scadenza non specificata.", "Info Data", JOptionPane.INFORMATION_MESSAGE);
                    // nuovaScadenza resta null, o si può impostare un default: LocalDate.now().plusDays(7);
                } else if (dataInput.isEmpty() && toDoDaModificare.getScadenza() != null) {
                    // L'utente ha cancellato la data, potremmo volerla impostare a null
                    // nuovaScadenza = null; // opzionale, in base al comportamento desiderato
                    JOptionPane.showMessageDialog(this, "Data di scadenza rimossa/non modificata.", "Info Data", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            // Passa l'indice, non l'oggetto ToDo, perché il controller si aspetta l'indice.
            controller.modificaToDo(bachecaSelezionata, selectedIndex, nuovoTitolo, nuovaDescrizione, nuovaScadenza, nuovoStato, currentSelectedColor[0]);
            aggiornaVistaCompletaBacheca();
        }
    }

    private void spostaToDoSelezionato(ActionEvent e) {
        int selectedIndex = todoList.getSelectedIndex(); // Indice dell'elemento selezionato
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
                // Passa l'indice dell'elemento da spostare
                controller.spostaToDoGUI(bachecaOrigine, selectedIndex, bachecaDestinazione);
                aggiornaVistaCompletaBacheca();
            }
        }
    }

    private void rimuoviToDo(ActionEvent e) {
        int selectedIndex = todoList.getSelectedIndex(); // Ottiene l'indice dell'elemento selezionato
        if (selectedIndex >= 0) {
            Titolo selezionato = (Titolo) bachecaSelector.getSelectedItem();
            if (selezionato != null) {
                controller.rimuoviToDo(selezionato, selectedIndex); // Passa l'indice
                aggiornaVistaCompletaBacheca();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleziona un ToDo da rimuovere.", "Attenzione", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void modificaDescrizioneBachecaSelezionata(ActionEvent e) {
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

        // Utilizzo di showInputDialog per semplicità
        Object input = JOptionPane.showInputDialog(
                this,
                "Modifica descrizione per la bacheca '" + bachecaSelezionata.name() + "':",
                "Modifica Descrizione Bacheca",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                descrizioneAttuale
        );
        // JOptionPane.showInputDialog restituisce null se l'utente preme Annulla o chiude la finestra
        // Altrimenti restituisce la stringa inserita (che può essere vuota se l'utente non scrive nulla e preme OK)
        if (input != null) { // L'utente ha premuto OK
            String nuovaDescrizioneIngresso = input.toString();
            controller.modificaDescrizioneBacheca(bachecaSelezionata, nuovaDescrizioneIngresso);
            aggiornaVistaCompletaBacheca();
        }
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
            // La finestra corrente (View) verrà chiusa dal controller se necessario,
            // o gestita nel riavvio del ciclo di autenticazione.
        }
    }
}

// Classe interna (o file separato) per il rendering personalizzato dei ToDo nella JList
class ToDoCellRenderer extends JLabel implements ListCellRenderer<ToDo> {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ToDoCellRenderer() {
        setOpaque(true); // Fondamentale perché il background color sia visibile
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ToDo> list, ToDo todo, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (todo != null) {
            String statoStr = todo.getStato() ? "[X]" : "[ ]";
            String scadenzaStr = (todo.getScadenza() != null) ? todo.getScadenza().format(dateFormatter) : "N/D";
            setText(String.format("%s %s (Scad: %s) - %s",
                    statoStr, todo.getTitolo(), scadenzaStr, todo.getDescrizione()));

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                // Imposta il colore di sfondo personalizzato se presente, altrimenti usa quello di default della lista
                setBackground(todo.getColore() != null ? todo.getColore() : list.getBackground());
                setForeground(list.getForeground()); // Usa il colore di testo di default
            }
        } else {
            // Caso in cui l'oggetto ToDo sia null (non dovrebbe accadere con DefaultListModel<ToDo>)
            setText("");
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }
}


