package gui;

import controller.Controller;
import org.ToDo.Bacheca; // Aggiornato
import org.ToDo.Titolo;  // Aggiornato
import org.ToDo.ToDo;    // Aggiornato

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

class NamedColor {
    public final String name;
    public final Color color;

    public NamedColor(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public String toString() { return name; }
    public Color getColor() { return color; }
    public String getName() { return name; }

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

    public static NamedColor findNamedColor(Color colorToFind) {
        if (colorToFind == null) return getPredefinedColors()[0];
        for (NamedColor namedColor : getPredefinedColors()) {
            if (namedColor.getColor().equals(colorToFind)) {
                return namedColor;
            }
        }
        return getPredefinedColors()[0];
    }
}

class ToDoCellRenderer extends JLabel implements ListCellRenderer<ToDo> {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ToDoCellRenderer() { setOpaque(true); }

    @Override
    public Component getListCellRendererComponent(JList<? extends ToDo> list, ToDo todo, int index, boolean isSelected, boolean cellHasFocus) {
        if (todo != null) {
            String statoStr = todo.getStato() ? "[X]" : "[ ]";
            String scadenzaStr = (todo.getScadenza() != null) ? todo.getScadenza().format(dateFormatter) : "N/D";
            setText(String.format("%s %s (Scad: %s) - %s", statoStr, todo.getTitolo(), scadenzaStr, todo.getDescrizione()));

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(todo.getColore() != null ? todo.getColore() : list.getBackground());
                if (todo.getColore() != null && isColorDark(todo.getColore())) {
                    setForeground(Color.WHITE);
                } else {
                    setForeground(list.getForeground());
                }
            }
        } else {
            setText("");
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }

    private boolean isColorDark(Color color) {
        if (color == null) return false;
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance < 0.5;
    }
}


public class View extends JFrame {
    private final JComboBox<Titolo> bachecaSelector;
    private final DefaultListModel<ToDo> todoListModel;
    private final JList<ToDo> todoList;
    private final JButton addToDoButton, removeToDoButton, modifyToDoButton, spostaToDoButton, logoutButton, modificaDescrizioneBachecaButton;
    private final JLabel descrizioneLabel;
    private final Controller controller;
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
        JComboBox<NamedColor> colorSelector = new JComboBox<>(predefinedColors);
        JPanel colorPreviewPanel = new JPanel();
        colorPreviewPanel.setBackground(predefinedColors[0].getColor());
        colorPreviewPanel.setPreferredSize(new Dimension(20, 20));
        colorSelector.addActionListener(ev -> {
            NamedColor selected = (NamedColor) colorSelector.getSelectedItem();
            if (selected != null) colorPreviewPanel.setBackground(selected.getColor());
        });

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Titolo:")); panel.add(titoloField);
        panel.add(new JLabel("Descrizione:")); panel.add(descrizioneField);
        panel.add(new JLabel("Data Scadenza (gg/mm/aaaa):")); panel.add(dataScadenzaField);
        panel.add(completatoCheckBox);
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanel.add(new JLabel("Colore:")); colorPanel.add(colorSelector); colorPanel.add(colorPreviewPanel);
        panel.add(colorPanel);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nuovo ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Titolo selezionato = (Titolo) bachecaSelector.getSelectedItem();
            if (selezionato != null && controller.getUtenteCorrente() != null) {
                String dataInput = dataScadenzaField.getText().trim();
                LocalDate scadenza = LocalDate.now().plusDays(7);
                try {
                    if (!dataInput.isEmpty()) scadenza = LocalDate.parse(dataInput, dateFormatter);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Formato data non valido. Verrà usata una data predefinita.", "Errore Data", JOptionPane.ERROR_MESSAGE);
                }

                ToDo nuovo = new ToDo(
                        titoloField.getText(),
                        descrizioneField.getText(),
                        scadenza,
                        completatoCheckBox.isSelected(),
                        "http://example.com",
                        ((NamedColor) colorSelector.getSelectedItem()).getColor(),
                        selezionato,
                        controller.getUtenteCorrente().getEmail()
                );
                controller.aggiungiToDo(selezionato, nuovo);
                aggiornaVistaCompletaBacheca();
            } else {
                JOptionPane.showMessageDialog(this, "Errore: Seleziona una bacheca.", "Errore", JOptionPane.ERROR_MESSAGE);
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

        JTextField titoloField = new JTextField(toDoDaModificare.getTitolo());
        JTextField descrizioneField = new JTextField(toDoDaModificare.getDescrizione());
        JTextField dataScadenzaField = new JTextField(toDoDaModificare.getScadenza() != null ? toDoDaModificare.getScadenza().format(dateFormatter) : "");
        JCheckBox completatoCheckBox = new JCheckBox("Completato", toDoDaModificare.getStato());
        JComboBox<NamedColor> colorSelectorModify = new JComboBox<>(predefinedColors);
        colorSelectorModify.setSelectedItem(NamedColor.findNamedColor(toDoDaModificare.getColore()));

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Titolo:")); panel.add(titoloField);
        panel.add(new JLabel("Descrizione:")); panel.add(descrizioneField);
        panel.add(new JLabel("Data Scadenza (gg/mm/aaaa):")); panel.add(dataScadenzaField);
        panel.add(completatoCheckBox);
        panel.add(new JLabel("Colore:")); panel.add(colorSelectorModify);

        int result = JOptionPane.showConfirmDialog(this, panel, "Modifica ToDo", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            LocalDate nuovaScadenza = toDoDaModificare.getScadenza();
            try {
                if (!dataScadenzaField.getText().trim().isEmpty()) nuovaScadenza = LocalDate.parse(dataScadenzaField.getText().trim(), dateFormatter);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Formato data non valido. La data non sarà modificata.", "Errore Data", JOptionPane.ERROR_MESSAGE);
            }

            controller.modificaToDo(
                    bachecaSelezionata,
                    selectedIndex,
                    titoloField.getText(),
                    descrizioneField.getText(),
                    nuovaScadenza,
                    completatoCheckBox.isSelected(),
                    ((NamedColor)colorSelectorModify.getSelectedItem()).getColor()
            );
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

        ArrayList<Titolo> opzioniDestinazioneList = new ArrayList<>(Arrays.asList(Titolo.values()));
        opzioniDestinazioneList.remove(bachecaOrigine);

        if (opzioniDestinazioneList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Non ci sono altre bacheche disponibili.", "Informazione", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Titolo bachecaDestinazione = (Titolo) JOptionPane.showInputDialog(this, "Sposta in bacheca:", "Sposta ToDo",
                JOptionPane.PLAIN_MESSAGE, null, opzioniDestinazioneList.toArray(), opzioniDestinazioneList.get(0));

        if (bachecaDestinazione != null) {
            controller.spostaToDoGUI(bachecaOrigine, selectedIndex, bachecaDestinazione);
            aggiornaVistaCompletaBacheca();
        }
    }

    private void rimuoviToDo(ActionEvent e) {
        int selectedIndex = todoList.getSelectedIndex();
        if (selectedIndex >= 0) {
            Titolo selezionato = (Titolo) bachecaSelector.getSelectedItem();
            controller.rimuoviToDo(selezionato, selectedIndex);
            aggiornaVistaCompletaBacheca();
        } else {
            JOptionPane.showMessageDialog(this, "Seleziona un ToDo da rimuovere.", "Attenzione", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void modificaDescrizioneBachecaSelezionata(ActionEvent e) {
        Titolo bachecaSelezionata = (Titolo) bachecaSelector.getSelectedItem();
        if (bachecaSelezionata == null) return;

        Bacheca bachecaCorrente = controller.getBacheche().get(bachecaSelezionata);
        String descrizioneAttuale = bachecaCorrente.getDescrizione();

        String nuovaDescrizione = (String) JOptionPane.showInputDialog(this, "Modifica descrizione per la bacheca '" + bachecaSelezionata.name() + "':",
                "Modifica Descrizione Bacheca", JOptionPane.PLAIN_MESSAGE, null, null, descrizioneAttuale);

        if (nuovaDescrizione != null) {
            controller.modificaDescrizioneBacheca(bachecaSelezionata, nuovaDescrizione);
            aggiornaVistaCompletaBacheca();
        }
    }

    private void performLogout(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(this, "Sei sicuro di voler effettuare il logout?", "Conferma Logout",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            controller.eseguiLogout();
        }
    }
}