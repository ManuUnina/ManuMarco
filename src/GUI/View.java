package gui;

import controller.Controller;
import org.ToDo.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

// Classe helper per associare un nome a un colore per la JComboBox
class NamedColor { // Assicurati che questa classe sia accessibile (es. file separato o interna statica)
    public final String name;
    public final Color color;

    public NamedColor(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public String toString() {
        return name; // Questo testo apparirà nella JComboBox
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    // Metodo statico per ottenere l'array di colori predefiniti
    public static NamedColor[] getPredefinedColors() {
        return new NamedColor[]{
                new NamedColor("Bianco", Color.WHITE),
                new NamedColor("Rosso Chiaro", new Color(255, 204, 203)),
                new NamedColor("Verde Chiaro", new Color(204, 255, 204)),
                new NamedColor("Blu Chiaro", new Color(204, 229, 255)),
                new NamedColor("Giallo Chiaro", new Color(255, 255, 204)),
                new NamedColor("Arancione Chiaro", new Color(255, 229, 204)),
                new NamedColor("Viola Chiaro", new Color(229, 204, 255))
        };
    }

    // Metodo per trovare un NamedColor per un dato Color (utile per pre-selezionare nella modifica)
    public static NamedColor findNamedColor(Color colorToFind) {
        if (colorToFind == null) {
            return getPredefinedColors()[0]; // Default a Bianco se il colore è null
        }
        for (NamedColor namedColor : getPredefinedColors()) {
            if (namedColor.getColor().equals(colorToFind)) {
                return namedColor;
            }
        }
        // Se il colore esistente non è uno dei predefiniti, ritorna Bianco come default
        // o gestisci come preferisci (es. mantieni il colore originale se possibile,
        // ma qui la JComboBox mostrerà solo i predefiniti).
        return getPredefinedColors()[0];
    }
}


public class View extends JFrame {
    private final JComboBox<Titolo> bachecaSelector;
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
    private final NamedColor[] predefinedColors = NamedColor.getPredefinedColors();

    public View(Controller controller) {
        this.controller = controller;

        setTitle("Gestione Bacheche - Utente: " + (controller.getUtenteCorrente() != null ? controller.getUtenteCorrente().getEmail() : "N/A"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        bachecaSelector = new JComboBox<>(Titolo.values());
        descrizioneLabel = new JLabel("Descrizione...");

        todoListModel = new DefaultListModel<>();
        todoList = new JList<>(todoListModel);
        todoList.setCellRenderer(new ToDoCellRenderer());

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

        // Componenti per la selezione del colore con JComboBox
        JComboBox<NamedColor> colorSelector = new JComboBox<>(predefinedColors);
        colorSelector.setSelectedIndex(0); // Default a Bianco

        // Preview del colore (opzionale ma utile)
        JPanel colorPreviewPanel = new JPanel();
        colorPreviewPanel.setBackground(predefinedColors[0].getColor());
        colorPreviewPanel.setPreferredSize(new Dimension(20, 20));
        colorSelector.addActionListener(ev -> {
            NamedColor selected = (NamedColor) colorSelector.getSelectedItem();
            if (selected != null) {
                colorPreviewPanel.setBackground(selected.getColor());
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
        colorPanel.add(new JLabel("Colore:"));
        colorPanel.add(colorSelector);
        colorPanel.add(colorPreviewPanel); // Aggiunge la preview
        panel.add(colorPanel);


        int result = JOptionPane.showConfirmDialog(this, panel, "Nuovo ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Titolo selezionato = (Titolo) bachecaSelector.getSelectedItem();
            if (selezionato != null && controller.getUtenteCorrente() != null) {
                String dataInput = dataScadenzaField.getText().trim();
                LocalDate scadenza = LocalDate.now().plusDays(7); // Default
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
                NamedColor namedColorSelected = (NamedColor) colorSelector.getSelectedItem();
                Color coloreScelto = (namedColorSelected != null) ? namedColorSelected.getColor() : Color.WHITE;

                ToDo nuovo = new ToDo(
                        titoloField.getText(),
                        descrizioneField.getText(),
                        controller.getUtenteCorrente().getEmail(),
                        scadenza,
                        "N/A", // Posizione, se necessaria
                        stato,
                        "http://example.com", // URL, se necessario
                        new ArrayList<>(), // Lista utenti, se necessaria
                        coloreScelto
                );
                controller.aggiungiToDo(selezionato, nuovo);
                aggiornaVistaCompletaBacheca();
            } else {
                JOptionPane.showMessageDialog(this, "Errore: Seleziona una bacheca e assicurati di essere loggato.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void modificaToDoSelezionato(ActionEvent e) {
        ToDo toDoDaModificare = todoList.getSelectedValue();
        int selectedIndex = todoList.getSelectedIndex();
        Titolo bachecaSelezionata = (Titolo) bachecaSelector.getSelectedItem();

        if (toDoDaModificare == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un ToDo da modificare.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (bachecaSelezionata == null) {
            JOptionPane.showMessageDialog(this, "Nessuna bacheca selezionata.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField titoloField = new JTextField(toDoDaModificare.getTitolo());
        JTextField descrizioneField = new JTextField(toDoDaModificare.getDescrizione());
        JTextField dataScadenzaField = new JTextField(toDoDaModificare.getScadenza() != null ? toDoDaModificare.getScadenza().format(dateFormatter) : "");
        JCheckBox completatoCheckBox = new JCheckBox("Completato", toDoDaModificare.getStato());

        // Componenti per la modifica del colore con JComboBox
        JComboBox<NamedColor> colorSelectorModify = new JComboBox<>(predefinedColors);
        NamedColor currentColor = NamedColor.findNamedColor(toDoDaModificare.getColore());
        colorSelectorModify.setSelectedItem(currentColor);

        // Preview del colore (opzionale ma utile)
        JPanel colorPreviewPanelModify = new JPanel();
        colorPreviewPanelModify.setBackground(currentColor.getColor());
        colorPreviewPanelModify.setPreferredSize(new Dimension(20, 20));
        colorSelectorModify.addActionListener(ev -> {
            NamedColor selected = (NamedColor) colorSelectorModify.getSelectedItem();
            if (selected != null) {
                colorPreviewPanelModify.setBackground(selected.getColor());
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
        colorPanelModify.add(new JLabel("Colore:"));
        colorPanelModify.add(colorSelectorModify);
        colorPanelModify.add(colorPreviewPanelModify); // Aggiunge la preview
        panel.add(colorPanelModify);


        int result = JOptionPane.showConfirmDialog(this, panel, "Modifica ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String nuovoTitolo = titoloField.getText();
            String nuovaDescrizione = descrizioneField.getText();
            String dataInput = dataScadenzaField.getText().trim();
            boolean nuovoStato = completatoCheckBox.isSelected();
            NamedColor namedColorSelected = (NamedColor) colorSelectorModify.getSelectedItem();
            Color nuovoColore = (namedColorSelected != null) ? namedColorSelected.getColor() : Color.WHITE;


            LocalDate nuovaScadenza = toDoDaModificare.getScadenza(); // Mantiene la vecchia se non modificata
            if (!dataInput.isEmpty()) {
                try {
                    nuovaScadenza = LocalDate.parse(dataInput, dateFormatter);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Formato data non valido. Usare gg/mm/aaaa.\nLa data di scadenza non sarà modificata.", "Errore Data", JOptionPane.ERROR_MESSAGE);
                }
            } else if (toDoDaModificare.getScadenza() != null && dataInput.isEmpty()){
                // L'utente ha cancellato la data, potremmo impostarla a null
                // Per ora, se il campo è vuoto, manteniamo la data esistente o null se era già null.
                // Se si vuole permettere di cancellare la data (rendendola null), si aggiunga:
                // nuovaScadenza = null;
                JOptionPane.showMessageDialog(this, "Il campo data è stato lasciato vuoto. La data di scadenza non è stata modificata o è stata rimossa se precedentemente impostata e ora il campo è vuoto.", "Info Data", JOptionPane.INFORMATION_MESSAGE);
            }


            controller.modificaToDo(bachecaSelezionata, selectedIndex, nuovoTitolo, nuovaDescrizione, nuovaScadenza, nuovoStato, nuovoColore);
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

        ArrayList<Titolo> opzioniDestinazioneList = new ArrayList<>(Arrays.asList(Titolo.values()));
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
                aggiornaVistaCompletaBacheca(); // Aggiorna la vista per riflettere lo spostamento
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

        Object input = JOptionPane.showInputDialog(
                this,
                "Modifica descrizione per la bacheca '" + bachecaSelezionata.name() + "':",
                "Modifica Descrizione Bacheca",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                descrizioneAttuale
        );
        if (input != null) {
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
        }
    }
}

// Classe interna (o file separato) per il rendering personalizzato dei ToDo nella JList
class ToDoCellRenderer extends JLabel implements ListCellRenderer<ToDo> {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ToDoCellRenderer() {
        setOpaque(true);
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
                setBackground(todo.getColore() != null ? todo.getColore() : list.getBackground());
                // Per garantire una buona leggibilità, potresti voler impostare il colore del testo
                // in base alla luminosità del colore di sfondo.
                // Ad esempio, se lo sfondo è scuro, il testo dovrebbe essere chiaro.
                // Qui usiamo un semplice default.
                if (todo.getColore() != null && isColorDark(todo.getColore()) && todo.getColore() != Color.WHITE) {
                    setForeground(Color.WHITE); // Testo bianco su sfondi scuri (escluso il bianco stesso)
                } else {
                    setForeground(list.getForeground()); // Colore testo di default
                }
            }
        } else {
            setText("");
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }
    // Metodo helper per determinare se un colore è "scuro"
    // Questa è una semplice implementazione, potresti volerne una più sofisticata.
    private boolean isColorDark(Color color) {
        if (color == null) return false;
        // Calcola la luminanza percepita
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance < 0.5; // Considera "scuro" se la luminanza è inferiore a 0.5
    }
}

