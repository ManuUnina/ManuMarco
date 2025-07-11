// File: src/GUI/View.java
package gui;

import controller.Controller;
import org.ToDo.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe helper per associare un nome a un colore per la JComboBox.
 * Deve essere accessibile (es. interna statica o file separato).
 */
class NamedColor {
    public final String name;
    public final Color color;

    public NamedColor(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public String toString() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    /**
     * Metodo statico per ottenere l'array di colori predefiniti.
     * @return Array di oggetti NamedColor predefiniti.
     */
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

    /**
     * Metodo per trovare un NamedColor per un dato Color.
     * Utile per pre-selezionare nella modifica.
     * @param colorToFind Il colore da cercare.
     * @return L'oggetto NamedColor corrispondente o il colore di default (Bianco) se non trovato.
     */
    public static NamedColor findNamedColor(Color colorToFind) {
        if (colorToFind == null) {
            return getPredefinedColors()[0];
        }
        for (NamedColor namedColor : getPredefinedColors()) {
            if (namedColor.getColor().equals(colorToFind)) {
                return namedColor;
            }
        }
        return getPredefinedColors()[0];
    }
}

public class View extends JFrame {
    private final Controller controller;

    private JPanel mainBoardsPanel;
    private JPanel boardSelectionContainer;

    private JPanel todoListPanel;
    private JSplitPane mainSplitPane;

    private JCheckBox showCompletedCheckBox;
    private DefaultListModel<ToDo> todoListModel; // Model per la JList
    private JList<ToDo> todoList;
    private JButton addToDoButton;
    private JLabel currentBoardDescriptionLabel;
    private JButton modifyBoardDescriptionButton;

    private Titolo selectedBoardTitle; // Mantiene traccia della bacheca attualmente selezionata

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NamedColor[] predefinedColors = NamedColor.getPredefinedColors();

    private boolean isTileView = false; // Stato per la visualizzazione a riquadri
    private JToggleButton viewToggleButton; // Pulsante per cambiare vista


    public View(Controller controller) {
        this.controller = controller;
        setTitle("Gestione ToDo - Utente: " + (controller.getUtenteCorrente() != null ? controller.getUtenteCorrente().getEmail() : "N/A"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        this.setLayout(new BorderLayout());

        JPanel topNavBarPanel = new JPanel();
        topNavBarPanel.setLayout(new BorderLayout());
        topNavBarPanel.setBackground(new Color(230, 230, 230));
        topNavBarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JLabel appNameLabel = new JLabel("  ToDo App");
        appNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topNavBarPanel.add(appNameLabel, BorderLayout.WEST);

        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        buttonContainer.setOpaque(false);

        JButton homeButton = new JButton("Home");
        homeButton.setFocusPainted(false); // Rimuove il bordo blu
        homeButton.addActionListener(e -> showBoardSelectionView());
        buttonContainer.add(homeButton);

        JButton profileButton = new JButton("Profilo");
        profileButton.setFocusPainted(false); // Rimuove il bordo blu
        profileButton.addActionListener(e -> showProfileDialog());
        buttonContainer.add(profileButton);

        JButton allIncompleteToDosButton = new JButton("ToDo"); // Nome cambiato
        allIncompleteToDosButton.setFocusPainted(false); // Rimuove il bordo blu
        allIncompleteToDosButton.addActionListener(e -> showAllIncompleteToDos());
        buttonContainer.add(allIncompleteToDosButton);

        JButton contactsButton = new JButton("Contatti");
        contactsButton.setFocusPainted(false);
        contactsButton.addActionListener(e -> showContactsDialog());
        buttonContainer.add(contactsButton);

        // Nuovo JToggleButton per la vista a riquadri
        viewToggleButton = new JToggleButton("Vista Riquadri");
        viewToggleButton.setFocusPainted(false); // Rimuove il bordo blu
        viewToggleButton.addActionListener(e -> {
            isTileView = viewToggleButton.isSelected(); // Aggiorna lo stato della vista
            refreshToDoList(); // Aggiorna la visualizzazione
        });
        buttonContainer.add(viewToggleButton);


        JButton logoutButtonTop = new JButton("Logout");
        logoutButtonTop.setForeground(Color.RED);
        logoutButtonTop.setFocusPainted(false); // Rimuove il bordo blu
        logoutButtonTop.addActionListener(this::performLogout);
        buttonContainer.add(logoutButtonTop);

        topNavBarPanel.add(buttonContainer, BorderLayout.EAST);
        this.add(topNavBarPanel, BorderLayout.NORTH);

        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerSize(0);
        mainSplitPane.setEnabled(false);
        mainSplitPane.setResizeWeight(0.0);

        boardSelectionContainer = new JPanel(new GridLayout(1, 3, 20, 0));
        boardSelectionContainer.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        boardSelectionContainer.setBackground(new Color(240, 240, 240));

        for (Titolo title : Titolo.values()) {
            boardSelectionContainer.add(createBoardPanel(title));
        }

        mainBoardsPanel = new JPanel(new GridBagLayout());
        mainBoardsPanel.add(boardSelectionContainer);

        mainSplitPane.setLeftComponent(mainBoardsPanel);

        todoListPanel = new JPanel(new BorderLayout());
        todoListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainSplitPane.setRightComponent(todoListPanel);

        this.add(mainSplitPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            mainSplitPane.setDividerLocation(1.0);
        });
    }

    private JPanel createBoardPanel(Titolo title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setPreferredSize(new Dimension(200, 150));

        JLabel titleLabel = new JLabel(title.name().replace("_", " "), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedBoardTitle = title;
                showToDoListForBoard();
            }
        });
        return panel;
    }

    /**
     * Configura il layout per mostrare la lista dei ToDo della bacheca selezionata.
     */
    private void showToDoListForBoard() {
        // Quando si entra nella schermata della lista ToDo, cambia il layout delle bacheche a verticale
        boardSelectionContainer.setLayout(new GridLayout(0, 1, 0, 20));
        boardSelectionContainer.revalidate();
        boardSelectionContainer.repaint();

        // Configura il JSplitPane per mostrare entrambi i componenti
        mainSplitPane.setDividerSize(0);
        mainSplitPane.setEnabled(false);
        mainSplitPane.setResizeWeight(0.35);
        mainSplitPane.setDividerLocation(0.35);

        // Popola il pannello della lista ToDo (o riquadri)
        todoListPanel.removeAll();
        todoListPanel.setLayout(new BorderLayout()); // Layout iniziale per i controlli in alto

        JPanel headerPanel = new JPanel(new BorderLayout());
        currentBoardDescriptionLabel = new JLabel("Descrizione: " + (selectedBoardTitle != null ? controller.getBacheche().get(selectedBoardTitle).getDescrizione() : "N/D"));
        modifyBoardDescriptionButton = new JButton("Modifica Descrizione");
        modifyBoardDescriptionButton.addActionListener(this::modificaDescrizioneBachecaSelezionata);

        JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        descriptionPanel.add(currentBoardDescriptionLabel);
        if (selectedBoardTitle != null) {
            descriptionPanel.add(modifyBoardDescriptionButton);
        }
        headerPanel.add(descriptionPanel, BorderLayout.NORTH);

        showCompletedCheckBox = new JCheckBox("Mostra ToDo Completati");
        showCompletedCheckBox.addActionListener(e -> refreshToDoList());
        headerPanel.add(showCompletedCheckBox, BorderLayout.SOUTH);

        todoListPanel.add(headerPanel, BorderLayout.NORTH);

        // Inizializza JList (o prepara per riquadri)
        todoListModel = new DefaultListModel<>();
        todoList = new JList<>(todoListModel);
        todoList.setCellRenderer(new ToDoCellRenderer());
        todoList.setFixedCellHeight(30); // Imposta altezza fissa per le celle della lista
        todoList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList<ToDo> list = (JList<ToDo>) e.getSource();
                int index = list.locationToIndex(e.getPoint());

                if (index != -1) {
                    Rectangle cellBounds = list.getCellBounds(index, index);

                    if (cellBounds != null && e.getY() >= cellBounds.y && e.getY() < (cellBounds.y + cellBounds.height)) {
                        ToDo todo = list.getModel().getElementAt(index);
                        int checkBoxGraphicClickWidth = 25;
                        if (e.getX() - cellBounds.x >= 0 && e.getX() - cellBounds.x < checkBoxGraphicClickWidth) {
                            controller.toggleToDoStatus(selectedBoardTitle, todo, !todo.getStato());
                            refreshToDoList(); // Aggiorna la vista dopo il toggle
                        } else {
                            showToDoDetailDialog(todo);
                        }
                    }
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addToDoButton = new JButton("<html><span style='font-size:1.2em;'>&#43;</span> Aggiungi ToDo</html>"); // Icona "+" Unicode
        addToDoButton.addActionListener(this::aggiungiToDo);
        buttonPanel.add(addToDoButton);

        todoListPanel.add(buttonPanel, BorderLayout.SOUTH);

        refreshToDoList(); // Chiamata per popolare la lista/riquadri in base a isTileView

        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    /**
     * Mostra tutti i ToDo non completati da tutte le bacheche.
     */
    private void showAllIncompleteToDos() {
        selectedBoardTitle = null; // Nessuna bacheca specifica selezionata

        // Assicurati che il layout delle bacheche sia verticale quando si visualizzano i ToDo
        boardSelectionContainer.setLayout(new GridLayout(0, 1, 0, 20));
        boardSelectionContainer.revalidate();
        boardSelectionContainer.repaint();

        // Configura il JSplitPane per mostrare entrambi i componenti
        mainSplitPane.setDividerSize(0);
        mainSplitPane.setEnabled(false);
        mainSplitPane.setResizeWeight(0.35);
        mainSplitPane.setDividerLocation(0.35);

        todoListPanel.removeAll();
        todoListPanel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        currentBoardDescriptionLabel = new JLabel("Descrizione: Tutti i ToDo Incompiuti"); // Titolo specifico per questa vista
        modifyBoardDescriptionButton = new JButton("Modifica Descrizione"); // Non aggiunto al pannello
        showCompletedCheckBox = new JCheckBox("Mostra ToDo Completati"); // Non aggiunto al pannello

        JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        descriptionPanel.add(currentBoardDescriptionLabel);
        headerPanel.add(descriptionPanel, BorderLayout.NORTH);

        todoListPanel.add(headerPanel, BorderLayout.NORTH);

        // Inizializza JList (o prepara per riquadri)
        todoListModel = new DefaultListModel<>();
        todoList = new JList<>(todoListModel);
        todoList.setCellRenderer(new ToDoCellRenderer());
        todoList.setFixedCellHeight(30); // Imposta altezza fissa per le celle della lista
        todoList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList<ToDo> list = (JList<ToDo>) e.getSource();
                int index = list.locationToIndex(e.getPoint());

                if (index != -1) {
                    Rectangle cellBounds = list.getCellBounds(index, index);
                    if (cellBounds != null && e.getY() >= cellBounds.y && e.getY() < (cellBounds.y + cellBounds.height)) {
                        ToDo todo = list.getModel().getElementAt(index);
                        int checkBoxGraphicClickWidth = 25;
                        if (e.getX() - cellBounds.x >= 0 && e.getX() - cellBounds.x < checkBoxGraphicClickWidth) {
                            controller.toggleToDoStatus(null, todo, !todo.getStato()); // TODO: gestire la bacheca di origine
                            refreshToDoList(); // Aggiorna la vista dopo il toggle
                        } else {
                            showToDoDetailDialog(todo);
                        }
                    }
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addToDoButton = new JButton("<html><span style='font-size:1.2em;'>&#43;</span> Aggiungi ToDo</html>");
        addToDoButton.addActionListener(e -> aggiungiToDoGlobale());
        buttonPanel.add(addToDoButton);

        todoListPanel.add(buttonPanel, BorderLayout.SOUTH);

        refreshToDoList(); // Popola la lista/riquadri in base a isTileView

        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    /**
     * Popola la lista con tutti i ToDo non completati da tutte le bacheche.
     */
    private void populateAllIncompleteToDos() {
        todoListModel.clear(); // Sempre pulisci il modello della JList
        if (isTileView) {
            // Se in vista riquadri, popola il pannello dei riquadri
            // Prima di aggiungere i riquadri, assicurati di rimuovere il JScrollPane o il precedente pannello
            Component centerComponent = ((BorderLayout)todoListPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (centerComponent instanceof JScrollPane) {
                todoListPanel.remove(centerComponent);
            } else if (centerComponent instanceof JPanel) { // Potrebbe essere il cardsPanel se si torna qui
                todoListPanel.remove(centerComponent);
            }


            JPanel cardsPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // 2 colonne per i riquadri
            cardsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding per i riquadri
            for (Bacheca bacheca : controller.getBacheche().values()) {
                for (ToDo t : bacheca.getToDos()) {
                    if (!t.getStato()) {
                        cardsPanel.add(createToDoCard(t));
                    }
                }
            }
            todoListPanel.add(new JScrollPane(cardsPanel), BorderLayout.CENTER);
        } else {
            // Se in vista lista, popola il modello della JList
            for (Bacheca bacheca : controller.getBacheche().values()) {
                for (ToDo t : bacheca.getToDos()) {
                    if (!t.getStato()) {
                        todoListModel.addElement(t);
                    }
                }
            }
            // Assicurati che la JList sia visualizzata
            Component centerComponent = ((BorderLayout)todoListPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
            if (!(centerComponent instanceof JScrollPane && ((JScrollPane)centerComponent).getViewport().getView() == todoList)) {
                if (centerComponent != null) {
                    todoListPanel.remove(centerComponent);
                }
                todoListPanel.add(new JScrollPane(todoList), BorderLayout.CENTER);
            }
        }
        todoListPanel.revalidate();
        todoListPanel.repaint();
    }


    /**
     * Gestisce l'aggiunta di un nuovo ToDo da una vista globale, permettendo la scelta della bacheca.
     */
    private void aggiungiToDoGlobale() {
        Titolo[] bachecheDisponibili = Titolo.values();
        JComboBox<Titolo> bachecaSelector = new JComboBox<>(bachecheDisponibili);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Seleziona la bacheca per il nuovo ToDo:"));
        panel.add(bachecaSelector);

        int result = JOptionPane.showConfirmDialog(this, panel, "Scegli Bacheca", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            selectedBoardTitle = (Titolo) bachecaSelector.getSelectedItem();
            aggiungiToDo(null);
        }
    }


    /**
     * Riporta la vista alla selezione delle bacheche.
     */
    private void showBoardSelectionView() {
        selectedBoardTitle = null;
        boardSelectionContainer.setLayout(new GridLayout(1, 3, 20, 0));
        boardSelectionContainer.revalidate();
        boardSelectionContainer.repaint();

        mainSplitPane.setDividerSize(0);
        mainSplitPane.setEnabled(false);
        mainSplitPane.setDividerLocation(1.0);
        mainSplitPane.revalidate();
        mainSplitPane.repaint();
    }

    /**
     * Aggiorna la lista dei ToDo visualizzati in base alla bacheca selezionata
     * e allo stato del checkbox "Mostra Completati".
     */
    public void refreshToDoList() {
        todoListPanel.removeAll(); // Pulisce il pannello prima di ridisegnare
        todoListPanel.setLayout(new BorderLayout()); // Layout per header e lista/riquadri

        JPanel headerPanel = new JPanel(new BorderLayout());
        // Aggiorna la descrizione in base alla bacheca selezionata o alla vista globale
        if (selectedBoardTitle == null) {
            currentBoardDescriptionLabel.setText("Descrizione: Tutti i ToDo Incompiuti");
        } else {
            currentBoardDescriptionLabel.setText("Descrizione: " + controller.getBacheche().get(selectedBoardTitle).getDescrizione());
        }

        JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        descriptionPanel.add(currentBoardDescriptionLabel);

        // Controllo per modifyBoardDescriptionButton e showCompletedCheckBox
        if (selectedBoardTitle != null) {
            descriptionPanel.add(modifyBoardDescriptionButton);
            headerPanel.add(showCompletedCheckBox, BorderLayout.SOUTH);
        } else {
            // Assicurati che non siano visibili se si è nella vista globale
            if (modifyBoardDescriptionButton != null) modifyBoardDescriptionButton.setVisible(false);
            if (showCompletedCheckBox != null) showCompletedCheckBox.setVisible(false);
        }

        headerPanel.add(descriptionPanel, BorderLayout.NORTH);
        todoListPanel.add(headerPanel, BorderLayout.NORTH);

        // Logica per cambiare tra vista lista e vista riquadri
        if (isTileView) {
            JPanel cardsContainer = new JPanel(new GridLayout(0, 2, 10, 10)); // 2 colonne per i riquadri, spaziatura 10px
            cardsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding per i riquadri

            // Popola i riquadri
            if (selectedBoardTitle == null) { // Tutti i ToDo incompiuti
                for (Bacheca bacheca : controller.getBacheche().values()) {
                    for (ToDo t : bacheca.getToDos()) {
                        if (!t.getStato()) {
                            cardsContainer.add(createToDoCard(t));
                        }
                    }
                }
            } else { // ToDo di una bacheca specifica
                Bacheca bacheca = controller.getBacheche().get(selectedBoardTitle);
                if (bacheca != null) {
                    for (ToDo t : bacheca.getToDos()) {
                        if (showCompletedCheckBox.isSelected() || !t.getStato()) {
                            cardsContainer.add(createToDoCard(t));
                        }
                    }
                }
            }
            todoListPanel.add(new JScrollPane(cardsContainer), BorderLayout.CENTER); // Aggiungi con scroll
        } else {
            // Vista lista (esistente)
            todoListModel.clear(); // Pulisci il modello prima di ripopolarlo
            if (selectedBoardTitle == null) { // Tutti i ToDo incompiuti
                for (Bacheca bacheca : controller.getBacheche().values()) {
                    for (ToDo t : bacheca.getToDos()) {
                        if (!t.getStato()) {
                            todoListModel.addElement(t);
                        }
                    }
                }
            } else { // ToDo di una bacheca specifica
                Bacheca bacheca = controller.getBacheche().get(selectedBoardTitle);
                if (bacheca != null) {
                    for (ToDo t : bacheca.getToDos()) {
                        if (showCompletedCheckBox.isSelected() || !t.getStato()) {
                            todoListModel.addElement(t);
                        }
                    }
                }
            }
            todoListPanel.add(new JScrollPane(todoList), BorderLayout.CENTER); // Aggiungi la JList con scroll
        }

        // Pannello bottoni (Aggiungi ToDo)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addToDoButton = new JButton("<html><span style='font-size:1.2em;'>&#43;</span> Aggiungi ToDo</html>");
        if (selectedBoardTitle == null) { // Azione per la vista globale
            addToDoButton.addActionListener(e -> aggiungiToDoGlobale());
        } else { // Azione per la bacheca specifica
            addToDoButton.addActionListener(e -> aggiungiToDo(null));
        }
        buttonPanel.add(addToDoButton);
        todoListPanel.add(buttonPanel, BorderLayout.SOUTH);

        todoListPanel.revalidate();
        todoListPanel.repaint();
    }


    /**
     * Crea un pannello (card) per un singolo ToDo nella visualizzazione a riquadri.
     * @param todo Il ToDo da visualizzare.
     * @return Un JPanel che rappresenta la card del ToDo.
     */
    private JPanel createToDoCard(ToDo todo) {
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS)); // Layout verticale per titolo/descrizione
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Bordo e padding
        cardPanel.setPreferredSize(new Dimension(180, 120)); // Dimensione preferita per la card
        cardPanel.setBackground(todo.getColore() != null ? todo.getColore() : Color.WHITE);

        // Aggiungi titolo e descrizione
        JLabel titleLabel = new JLabel("<html><b>" + todo.getTitolo() + "</b></html>");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Centra il testo
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel descLabel = new JLabel("<html>" + todo.getDescrizione() + "</html>");
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Colore del testo per leggibilità
        Color bgColor = todo.getColore();
        if (bgColor != null && ToDoCellRenderer.isColorDark(bgColor) && !bgColor.equals(Color.WHITE)) { // Chiamata statica
            titleLabel.setForeground(Color.WHITE);
            descLabel.setForeground(Color.WHITE);
        } else {
            titleLabel.setForeground(Color.BLACK);
            descLabel.setForeground(Color.BLACK);
        }


        cardPanel.add(titleLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spazio tra titolo e descrizione
        cardPanel.add(descLabel);

        // Listener per aprire il dettaglio ToDo al click sulla card
        cardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showToDoDetailDialog(todo);
            }
        });

        return cardPanel;
    }


    /**
     * Mostra un dialog con i dettagli di un ToDo selezionato.
     * @param todo Il ToDo di cui mostrare i dettagli.
     */
    private void showToDoDetailDialog(ToDo todo) {
        JDialog detailDialog = new JDialog(this, "Dettagli ToDo: " + todo.getTitolo(), true);
        detailDialog.setSize(500, 450);
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.setResizable(false);

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("<html><b>Titolo:</b> " + todo.getTitolo() + "</html>");
        JLabel descLabel = new JLabel("<html><b>Descrizione:</b> " + todo.getDescrizione() + "</html>");
        JLabel authorLabel = new JLabel("<html><b>Autore:</b> " + todo.getListaUtenti().getAutore() + "</html>");
        JLabel sharedUsersLabel = new JLabel("<html><b>Condiviso con:</b> " + (todo.getListaUtenti().getLista().isEmpty() ? "Nessuno" : String.join(", ", todo.getListaUtenti().getLista())) + "</html>");
        JLabel statusLabel = new JLabel("<html><b>Stato:</b> " + (todo.getStato() ? "Completato" : "Incompleto") + "</html>");
        JLabel dueDateLabel = new JLabel("<html><b>Scadenza:</b> " + (todo.getScadenza() != null ? todo.getScadenza().format(dateFormatter) : "N/D") + "</html>");

        JLabel urlLabel = new JLabel("<html><b>URL:</b> <a href=\"" + (todo.getUrl() != null && !todo.getUrl().isEmpty() ? todo.getUrl() : "#") + "\">" + (todo.getUrl() != null && !todo.getUrl().isEmpty() ? todo.getUrl() : "N/D") + "</a></html>");
        if (todo.getUrl() != null && !todo.getUrl().isEmpty()) {
            urlLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            urlLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(new URI(todo.getUrl()));
                        } else {
                            JOptionPane.showMessageDialog(detailDialog, "Il browser non può essere aperto automaticamente.", "Errore", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (URISyntaxException | IOException ex) {
                        JOptionPane.showMessageDialog(detailDialog, "Errore nell'apertura dell'URL: " + ex.getMessage(), "Errore URL", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }

        JLabel posizioneLabel = new JLabel("<html><b>Posizione:</b> " + (todo.getPosizione() != null && !todo.getPosizione().isEmpty() ? todo.getPosizione().trim() : "N/D") + "</html>");


        String hexColor = String.format("%06x", todo.getColore().getRGB() & 0xFFFFFF);
        JLabel colorLabel = new JLabel("<html><b>Colore:</b> <font color='#" + hexColor + "'>&#9632;</font> " + NamedColor.findNamedColor(todo.getColore()).getName() + "</html>");

        infoPanel.add(titleLabel);
        infoPanel.add(descLabel);
        infoPanel.add(authorLabel);
        infoPanel.add(sharedUsersLabel);
        infoPanel.add(statusLabel);
        infoPanel.add(dueDateLabel);
        infoPanel.add(urlLabel);
        infoPanel.add(posizioneLabel);
        infoPanel.add(colorLabel);

        if (todo.getImmagine() != null) {
            ImageIcon imageIcon = new ImageIcon(todo.getImmagine());
            Image image = imageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            infoPanel.add(imageLabel);
        }


        detailDialog.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton modifyButton = new JButton("Modifica");
        modifyButton.addActionListener(e -> {
            detailDialog.dispose();
            modificaToDo(todo);
        });
        JButton moveButton = new JButton("Sposta");
        moveButton.addActionListener(e -> {
            detailDialog.dispose();
            spostaToDo(todo);
        });
        JButton deleteButton = new JButton("Elimina");
        deleteButton.addActionListener(e -> {
            detailDialog.dispose();
            rimuoviToDo(todo);
        });
        JButton shareButton = new JButton("Condividi");
        shareButton.addActionListener(e -> {
            detailDialog.dispose();
            condividiToDo(todo);
        });
        JButton toggleStatusButton = new JButton(todo.getStato() ? "Segna Incompleto" : "Segna Completato");
        toggleStatusButton.addActionListener(e -> {
            detailDialog.dispose();
            controller.toggleToDoStatus(selectedBoardTitle, todo, !todo.getStato());
        });

        JButton closeButton = new JButton("Chiudi");
        closeButton.addActionListener(e -> detailDialog.dispose());

        buttonPanel.add(modifyButton);
        buttonPanel.add(moveButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(shareButton);
        buttonPanel.add(toggleStatusButton);
        buttonPanel.add(closeButton);

        detailDialog.add(buttonPanel, BorderLayout.SOUTH);
        detailDialog.setVisible(true);
    }

    /**
     * Gestisce l'aggiunta di un nuovo ToDo.
     * @param e L'evento dell'azione.
     */
    private void aggiungiToDo(ActionEvent e) {
        if (selectedBoardTitle == null) {
            JOptionPane.showMessageDialog(this, "Seleziona una bacheca prima di aggiungere un ToDo.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField titoloField = new JTextField();
        JTextField descrizioneField = new JTextField();
        JTextField dataScadenzaField = new JTextField(10);
        JTextField urlField = new JTextField();
        JTextField posizioneField = new JTextField();
        JComboBox<NamedColor> colorSelector = new JComboBox<>(predefinedColors);
        colorSelector.setSelectedIndex(0);

        final byte[][] immagineSelezionata = {null};
        JButton allegaImmagineButton = new JButton("Allega Immagine");
        allegaImmagineButton.addActionListener(ev -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    immagineSelezionata[0] = Files.readAllBytes(file.toPath());
                    JOptionPane.showMessageDialog(this, "Immagine allegata con successo!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Errore nella lettura dell'immagine.", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

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
        panel.add(new JLabel("URL (opzionale):"));
        panel.add(urlField);
        panel.add(new JLabel("Posizione (opzionale):"));
        panel.add(posizioneField);
        panel.add(allegaImmagineButton);


        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanel.add(new JLabel("Colore:"));
        colorPanel.add(colorSelector);
        colorPanel.add(colorPreviewPanel);
        panel.add(colorPanel);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nuovo ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if (controller.getUtenteCorrente() != null) {
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

                NamedColor namedColorSelected = (NamedColor) colorSelector.getSelectedItem();
                Color coloreScelto = (namedColorSelected != null) ? namedColorSelected.getColor() : Color.WHITE;
                ToDo nuovo = new ToDo(
                        titoloField.getText(),
                        descrizioneField.getText(),
                        scadenza,
                        false,
                        urlField.getText().trim(),
                        posizioneField.getText().trim(),
                        coloreScelto,
                        immagineSelezionata[0],
                        selectedBoardTitle,
                        controller.getUtenteCorrente().getEmail()
                );
                controller.aggiungiToDo(selectedBoardTitle, nuovo);
                refreshToDoList();
            } else {
                JOptionPane.showMessageDialog(this, "Errore: Assicurati di essere loggato.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void modificaToDo(ToDo toDoDaModificare) {
        JTextField titoloField = new JTextField(toDoDaModificare.getTitolo());
        JTextField descrizioneField = new JTextField(toDoDaModificare.getDescrizione());
        JTextField dataScadenzaField = new JTextField(toDoDaModificare.getScadenza() != null ? toDoDaModificare.getScadenza().format(dateFormatter) : "");
        JTextField urlField = new JTextField(toDoDaModificare.getUrl());
        JTextField posizioneField = new JTextField(toDoDaModificare.getPosizione());
        JCheckBox completatoCheckBoxDialog = new JCheckBox("Completato", toDoDaModificare.getStato());

        JComboBox<NamedColor> colorSelectorModify = new JComboBox<>(predefinedColors);
        NamedColor currentColor = NamedColor.findNamedColor(toDoDaModificare.getColore());
        colorSelectorModify.setSelectedItem(currentColor);

        JPanel colorPreviewPanelModify = new JPanel();
        colorPreviewPanelModify.setBackground(currentColor.getColor());
        colorPreviewPanelModify.setPreferredSize(new Dimension(20, 20));
        colorSelectorModify.addActionListener(ev -> {
            NamedColor selected = (NamedColor) colorSelectorModify.getSelectedItem();
            if (selected != null) {
                colorPreviewPanelModify.setBackground(selected.getColor());
            }
        });

        JButton attachImageButton = new JButton("Allega Immagine");
        final byte[][] nuovaImmagine = {toDoDaModificare.getImmagine()};
        attachImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int resultFileChooser = fileChooser.showOpenDialog(this);
            if (resultFileChooser == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    nuovaImmagine[0] = Files.readAllBytes(selectedFile.toPath());
                    JOptionPane.showMessageDialog(this, "Immagine allegata con successo.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Errore durante la lettura dell'immagine.", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Titolo:"));
        panel.add(titoloField);
        panel.add(new JLabel("Descrizione:"));
        panel.add(descrizioneField);
        panel.add(new JLabel("Data Scadenza (gg/mm/aaaa):"));
        panel.add(dataScadenzaField);
        panel.add(new JLabel("URL (opzionale):"));
        panel.add(urlField);
        panel.add(new JLabel("Posizione (opzionale):"));
        panel.add(posizioneField);
        panel.add(completatoCheckBoxDialog);
        panel.add(attachImageButton);

        JPanel colorPanelModify = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanelModify.add(new JLabel("Colore:"));
        colorPanelModify.add(colorSelectorModify);
        colorPanelModify.add(colorPreviewPanelModify);
        panel.add(colorPanelModify);

        int result = JOptionPane.showConfirmDialog(this, panel, "Modifica ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String nuovoTitolo = titoloField.getText();
            String nuovaDescrizione = descrizioneField.getText();
            String dataInput = dataScadenzaField.getText().trim();
            String nuovoUrl = urlField.getText().trim();
            String nuovaPosizione = posizioneField.getText().trim();
            boolean nuovoStato = completatoCheckBoxDialog.isSelected();
            NamedColor namedColorSelected = (NamedColor) colorSelectorModify.getSelectedItem();
            Color nuovoColore = (namedColorSelected != null) ? namedColorSelected.getColor() : Color.WHITE;

            LocalDate nuovaScadenza = toDoDaModificare.getScadenza();
            if (!dataInput.isEmpty()) {
                try {
                    nuovaScadenza = LocalDate.parse(dataInput, dateFormatter);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Formato data non valido. Usare gg/mm/aaaa.\nLa data di scadenza non sarà modificata.", "Errore Data", JOptionPane.ERROR_MESSAGE);
                }
            } else if (toDoDaModificare.getScadenza() != null && dataInput.isEmpty()){
                nuovaScadenza = null;
                JOptionPane.showMessageDialog(this, "Il campo data è stato lasciato vuoto. La data di scadenza è stata rimossa.", "Info Data", JOptionPane.INFORMATION_MESSAGE);
            }

            controller.modificaToDo(selectedBoardTitle, toDoDaModificare, nuovoTitolo, nuovaDescrizione, nuovaScadenza, nuovoStato, nuovoColore, nuovoUrl, nuovaPosizione, toDoDaModificare.getImmagine());
        }
    }

    private void spostaToDo(ToDo toDoDaSpostare) {
        if (toDoDaSpostare == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un ToDo da spostare.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<Titolo> opzioniDestinazioneList = new ArrayList<>(Arrays.asList(Titolo.values()));
        opzioniDestinazioneList.remove(selectedBoardTitle);

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
                controller.spostaToDoGUI(selectedBoardTitle, toDoDaSpostare, bachecaDestinazione);
            }
        }
    }

    private void rimuoviToDo(ToDo toDoDaRimuovere) {
        if (toDoDaRimuovere == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un ToDo da rimuovere.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Sei sicuro di voler eliminare questo ToDo?", "Conferma Eliminazione", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            controller.rimuoviToDo(selectedBoardTitle, toDoDaRimuovere);
        }
    }

    private void condividiToDo(ToDo todo) {
        ListaUtenti contatti = controller.getContatti();
        if (contatti == null || contatti.getLista().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Non hai contatti da cui scegliere. Aggiungine uno dalla sezione 'Contatti'.", "Nessun Contatto", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Seleziona i contatti con cui condividere:"));

        List<JCheckBox> checkBoxes = new ArrayList<>();
        for (String email : contatti.getLista()) {
            JCheckBox checkBox = new JCheckBox(email);
            checkBoxes.add(checkBox);
            panel.add(checkBox);
        }

        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(panel), "Condividi con i Contatti", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            for (JCheckBox checkBox : checkBoxes) {
                if (checkBox.isSelected()) {
                    controller.condividiToDo(todo, checkBox.getText());
                }
            }
            refreshToDoList();
        }
    }


    private void modificaDescrizioneBachecaSelezionata(ActionEvent e) {
        if (selectedBoardTitle == null) {
            JOptionPane.showMessageDialog(this, "Seleziona una bacheca per modificarne la descrizione.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Bacheca bachecaCorrente = controller.getBacheche().get(selectedBoardTitle);
        if (bachecaCorrente == null) {
            JOptionPane.showMessageDialog(this, "Errore: Bacheca non trovata.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String descrizioneAttuale = bachecaCorrente.getDescrizione();

        Object input = JOptionPane.showInputDialog(
                this,
                "Modifica descrizione per la bacheca '" + selectedBoardTitle.name() + "':",
                "Modifica Descrizione Bacheca",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                descrizioneAttuale
        );
        if (input != null) {
            String nuovaDescrizioneIngresso = input.toString();
            controller.modificaDescrizioneBacheca(selectedBoardTitle, nuovaDescrizioneIngresso);
            refreshToDoList();
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

    private void showProfileDialog() {
        String userEmail = controller.getUtenteCorrente() != null ? controller.getUtenteCorrente().getEmail() : "N/A";
        JOptionPane.showMessageDialog(this,
                "Email Utente: " + userEmail,
                "Profilo Personale",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void refreshContatti() {
        // This method can be expanded to update the contacts dialog if it's open
        System.out.println("Contact list refreshed.");
    }

    private void showContactsDialog() {
        JDialog contactsDialog = new JDialog(this, "Gestione Contatti", true);
        contactsDialog.setSize(400, 300);
        contactsDialog.setLocationRelativeTo(this);
        contactsDialog.setLayout(new BorderLayout());

        DefaultListModel<String> contactsListModel = new DefaultListModel<>();
        JList<String> contactsList = new JList<>(contactsListModel);

        // Populate the list with existing contacts
        ListaUtenti contatti = controller.getContatti();
        if (contatti != null) {
            for (String contact : contatti.getLista()) {
                contactsListModel.addElement(contact);
            }
        }


        JScrollPane scrollPane = new JScrollPane(contactsList);
        contactsDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Aggiungi Contatto");
        addButton.addActionListener(e -> {
            String email = JOptionPane.showInputDialog(contactsDialog, "Inserisci l'email del contatto:");
            if (email != null && !email.trim().isEmpty()) {
                controller.aggiungiContatto(email.trim());
                // Refresh the model to show the new contact
                contactsListModel.removeAllElements();
                for (String contact : controller.getContatti().getLista()) {
                    contactsListModel.addElement(contact);
                }
            }
        });

        JButton removeButton = new JButton("Rimuovi Contatto");
        removeButton.addActionListener(e -> {
            String selectedContact = contactsList.getSelectedValue();
            if (selectedContact != null) {
                int confirm = JOptionPane.showConfirmDialog(contactsDialog, "Sei sicuro di voler rimuovere questo contatto?", "Conferma Rimozione", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    controller.rimuoviContatto(selectedContact);
                    // Refresh the model to remove the contact
                    contactsListModel.removeElement(selectedContact);
                }
            } else {
                JOptionPane.showMessageDialog(contactsDialog, "Seleziona un contatto da rimuovere.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);

        contactsDialog.add(buttonPanel, BorderLayout.SOUTH);
        contactsDialog.setVisible(true);
    }


    public static class ToDoCellRenderer extends JCheckBox implements ListCellRenderer<ToDo> {
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        public ToDoCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ToDo> list, ToDo todo, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (todo != null) {
                setSelected(todo.getStato());
                String scadenzaStr = (todo.getScadenza() != null) ? todo.getScadenza().format(dateFormatter) : "N/D";
                setText(String.format("%s (Scad: %s) - %s",
                        todo.getTitolo(), scadenzaStr, todo.getDescrizione()));

                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    Color bgColor = todo.getColore() != null ? todo.getColore() : list.getBackground();
                    setBackground(bgColor);
                    if (bgColor != null && ToDoCellRenderer.isColorDark(bgColor) && !bgColor.equals(Color.WHITE)) {
                        setForeground(Color.WHITE);
                    } else {
                        setForeground(Color.BLACK);
                    }
                }
                setEnabled(list.isEnabled());
                setFont(list.getFont());

            } else {
                setText("");
                setSelected(false);
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

        // Metodo reso statico
        private static boolean isColorDark(Color color) {
            if (color == null) return false;
            double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
            return luminance < 0.5;
        }
    }
}