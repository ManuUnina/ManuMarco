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
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final transient Controller controller;

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

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NamedColor[] predefinedColors = NamedColor.getPredefinedColors();

    private boolean isTileView = false; // Stato per la visualizzazione a riquadri
    private JToggleButton viewToggleButton; // Pulsante per cambiare vista

    // Logger and Constants
    private static final Logger LOGGER = Logger.getLogger(View.class.getName());
    private static final String ERROR_TITLE = "Errore";
    private static final String MODIFY_BOARD_DESC_TITLE = "Modifica Descrizione Bacheca";
    private static final String ADD_TODO_BUTTON_TEXT = "<html><span style='font-size:1.2em;'>&#43;</span> Aggiungi ToDo</html>";
    private static final String SHOW_COMPLETED_CHECKBOX_TEXT = "Mostra ToDo Completati";
    private static final String MODIFY_DESCRIPTION_BUTTON_TEXT = "Modifica Descrizione";


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

        boardSelectionContainer = new JPanel(new GridLayout(1, 4, 20, 0)); // Aumentato a 4 per la nuova bacheca
        boardSelectionContainer.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        boardSelectionContainer.setBackground(new Color(240, 240, 240));

        for (Titolo title : Titolo.values()) {
            boardSelectionContainer.add(createBoardPanel(title));
        }
        boardSelectionContainer.add(createSharedBoardPanel());

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

    private JPanel createSharedBoardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2)); // Bordo evidenziato
        panel.setBackground(new Color(255, 248, 225)); // Sfondo leggermente diverso
        panel.setPreferredSize(new Dimension(200, 150));

        JLabel titleLabel = new JLabel("Condivisi con me", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showSharedToDoList();
            }
        });
        return panel;
    }

    private void showSharedToDoList() {
        selectedBoardTitle = null;

        mainSplitPane.setDividerSize(0);
        mainSplitPane.setEnabled(false);
        mainSplitPane.setResizeWeight(0.35);
        mainSplitPane.setDividerLocation(0.35);

        todoListPanel.removeAll();
        todoListPanel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        currentBoardDescriptionLabel = new JLabel("Descrizione: ToDo condivisi da altri utenti");
        headerPanel.add(currentBoardDescriptionLabel, BorderLayout.NORTH);

        modifyBoardDescriptionButton = new JButton(MODIFY_DESCRIPTION_BUTTON_TEXT);
        addToDoButton = new JButton(ADD_TODO_BUTTON_TEXT);

        modifyBoardDescriptionButton.setVisible(false);
        addToDoButton.setVisible(false);

        todoListPanel.add(headerPanel, BorderLayout.NORTH);

        todoListModel = new DefaultListModel<>();
        todoList = new JList<>(todoListModel);
        todoList.setCellRenderer(new ToDoCellRenderer());
        todoList.setFixedCellHeight(30);

        todoList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList<ToDo> list = (JList<ToDo>) e.getSource();
                int index = list.locationToIndex(e.getPoint());

                if (index != -1) {
                    ToDo todo = list.getModel().getElementAt(index);
                    showToDoDetailDialog(todo);
                }
            }
        });

        Bacheca bachecaCondivisi = controller.getBachecaCondivisi();
        if (bachecaCondivisi != null) {
            for (ToDo todo : bachecaCondivisi.getToDos()) {
                todoListModel.addElement(todo);
            }
        }
        todoListPanel.add(new JScrollPane(todoList), BorderLayout.CENTER);

        todoListPanel.revalidate();
        todoListPanel.repaint();
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
        modifyBoardDescriptionButton = new JButton(MODIFY_DESCRIPTION_BUTTON_TEXT);
        modifyBoardDescriptionButton.addActionListener(this::modificaDescrizioneBachecaSelezionata);

        JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        descriptionPanel.add(currentBoardDescriptionLabel);
        if (selectedBoardTitle != null) {
            descriptionPanel.add(modifyBoardDescriptionButton);
        }
        headerPanel.add(descriptionPanel, BorderLayout.NORTH);

        showCompletedCheckBox = new JCheckBox(SHOW_COMPLETED_CHECKBOX_TEXT);
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
                            controller.toggleToDoStatus(selectedBoardTitle, todo, !Boolean.TRUE.equals(todo.getStato()));
                            refreshToDoList(); // Aggiorna la vista dopo il toggle
                        } else {
                            showToDoDetailDialog(todo);
                        }
                    }
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addToDoButton = new JButton(ADD_TODO_BUTTON_TEXT);
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
        modifyBoardDescriptionButton = new JButton(MODIFY_DESCRIPTION_BUTTON_TEXT); // Non aggiunto al pannello
        showCompletedCheckBox = new JCheckBox(SHOW_COMPLETED_CHECKBOX_TEXT); // Non aggiunto al pannello

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
                            controller.toggleToDoStatus(null, todo, !Boolean.TRUE.equals(todo.getStato()));
                            refreshToDoList(); // Aggiorna la vista dopo il toggle
                        } else {
                            showToDoDetailDialog(todo);
                        }
                    }
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addToDoButton = new JButton(ADD_TODO_BUTTON_TEXT);
        addToDoButton.addActionListener(e -> aggiungiToDoGlobale());
        buttonPanel.add(addToDoButton);

        todoListPanel.add(buttonPanel, BorderLayout.SOUTH);

        refreshToDoList(); // Popola la lista/riquadri in base a isTileView

        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    /**
     * Aggiorna la lista dei ToDo visualizzati in base alla bacheca selezionata
     * e allo stato del checkbox "Mostra Completati".
     */
    public void refreshToDoList() {
        todoListPanel.removeAll();
        todoListPanel.setLayout(new BorderLayout());

        // Setup Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        String descriptionText = (selectedBoardTitle == null)
                ? "Descrizione: Tutti i ToDo Incompiuti"
                : "Descrizione: " + controller.getBacheche().get(selectedBoardTitle).getDescrizione();
        currentBoardDescriptionLabel = new JLabel(descriptionText);

        JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        descriptionPanel.add(currentBoardDescriptionLabel);

        if (selectedBoardTitle != null) {
            modifyBoardDescriptionButton = new JButton(MODIFY_DESCRIPTION_BUTTON_TEXT);
            modifyBoardDescriptionButton.addActionListener(this::modificaDescrizioneBachecaSelezionata);
            descriptionPanel.add(modifyBoardDescriptionButton);

            showCompletedCheckBox = new JCheckBox(SHOW_COMPLETED_CHECKBOX_TEXT);
            showCompletedCheckBox.addActionListener(e -> refreshToDoList());
            headerPanel.add(showCompletedCheckBox, BorderLayout.SOUTH);
        }
        headerPanel.add(descriptionPanel, BorderLayout.NORTH);
        todoListPanel.add(headerPanel, BorderLayout.NORTH);

        // Populate content (List or Tiles)
        if (isTileView) {
            populateTileView();
        } else {
            populateListView();
        }

        // Setup Footer Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addToDoButton = new JButton(ADD_TODO_BUTTON_TEXT);
        if (selectedBoardTitle == null) {
            addToDoButton.addActionListener(e -> aggiungiToDoGlobale());
        } else {
            addToDoButton.addActionListener(this::aggiungiToDo);
        }
        buttonPanel.add(addToDoButton);
        todoListPanel.add(buttonPanel, BorderLayout.SOUTH);

        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    private void populateTileView() {
        JPanel cardsContainer = new JPanel(new GridLayout(0, 2, 10, 10));
        cardsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (selectedBoardTitle == null) { // All incomplete ToDos view
            for (Bacheca bacheca : controller.getBacheche().values()) {
                for (ToDo t : bacheca.getToDos()) {
                    if (!Boolean.TRUE.equals(t.getStato())) {
                        cardsContainer.add(createToDoCard(t));
                    }
                }
            }
        } else { // Specific board view
            Bacheca bacheca = controller.getBacheche().get(selectedBoardTitle);
            if (bacheca != null) {
                for (ToDo t : bacheca.getToDos()) {
                    if (showCompletedCheckBox.isSelected() || !Boolean.TRUE.equals(t.getStato())) {
                        cardsContainer.add(createToDoCard(t));
                    }
                }
            }
        }
        todoListPanel.add(new JScrollPane(cardsContainer), BorderLayout.CENTER);
    }

    private void populateListView() {
        todoListModel.clear();
        if (selectedBoardTitle == null) { // All incomplete ToDos view
            for (Bacheca bacheca : controller.getBacheche().values()) {
                for (ToDo t : bacheca.getToDos()) {
                    if (!Boolean.TRUE.equals(t.getStato())) {
                        todoListModel.addElement(t);
                    }
                }
            }
        } else { // Specific board view
            Bacheca bacheca = controller.getBacheche().get(selectedBoardTitle);
            if (bacheca != null) {
                for (ToDo t : bacheca.getToDos()) {
                    if (showCompletedCheckBox.isSelected() || !Boolean.TRUE.equals(t.getStato())) {
                        todoListModel.addElement(t);
                    }
                }
            }
        }
        todoListPanel.add(new JScrollPane(todoList), BorderLayout.CENTER);
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
        boardSelectionContainer.setLayout(new GridLayout(1, 4, 20, 0)); // Aumentato a 4
        boardSelectionContainer.revalidate();
        boardSelectionContainer.repaint();

        mainSplitPane.setDividerSize(0);
        mainSplitPane.setEnabled(false);
        mainSplitPane.setDividerLocation(1.0);
        mainSplitPane.revalidate();
        mainSplitPane.repaint();
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
     * Mostra un dialog con i dettagli di un ToDo selezionato. (Refactored "Brain Method")
     * @param todo Il ToDo di cui mostrare i dettagli.
     */
    private void showToDoDetailDialog(ToDo todo) {
        JDialog detailDialog = new JDialog(this, "Dettagli ToDo: " + todo.getTitolo(), true);
        detailDialog.setSize(500, 450);
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.setResizable(false);

        JPanel infoPanel = createToDoDetailInfoPanel(todo, detailDialog);
        JPanel buttonPanel = createToDoDetailButtonPanel(todo, detailDialog);

        detailDialog.add(infoPanel, BorderLayout.CENTER);
        detailDialog.add(buttonPanel, BorderLayout.SOUTH);
        detailDialog.setVisible(true);
    }

    private JPanel createToDoDetailInfoPanel(ToDo todo, JDialog parentDialog) {
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoPanel.add(new JLabel("<html><b>Titolo:</b> " + todo.getTitolo() + "</html>"));
        infoPanel.add(new JLabel("<html><b>Descrizione:</b> " + todo.getDescrizione() + "</html>"));
        infoPanel.add(new JLabel("<html><b>Autore:</b> " + todo.getListaUtenti().getAutore() + "</html>"));
        infoPanel.add(new JLabel("<html><b>Condiviso con:</b> " + (todo.getListaUtenti().getLista().isEmpty() ? "Nessuno" : String.join(", ", todo.getListaUtenti().getLista())) + "</html>"));
        infoPanel.add(new JLabel("<html><b>Stato:</b> " + (Boolean.TRUE.equals(todo.getStato()) ? "Completato" : "Incompleto") + "</html>"));
        infoPanel.add(new JLabel("<html><b>Scadenza:</b> " + (todo.getScadenza() != null ? todo.getScadenza().format(dateFormatter) : "N/D") + "</html>"));

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
                            JOptionPane.showMessageDialog(parentDialog, "Il browser non può essere aperto automaticamente.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (URISyntaxException | IOException ex) {
                        JOptionPane.showMessageDialog(parentDialog, "Errore nell'apertura dell'URL: " + ex.getMessage(), "Errore URL", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
        infoPanel.add(urlLabel);
        infoPanel.add(new JLabel("<html><b>Posizione:</b> " + (todo.getPosizione() != null && !todo.getPosizione().isEmpty() ? todo.getPosizione().trim() : "N/D") + "</html>"));

        String hexColor = String.format("%06x", todo.getColore().getRGB() & 0xFFFFFF);
        infoPanel.add(new JLabel("<html><b>Colore:</b> <font color='#" + hexColor + "'>&#9632;</font> " + NamedColor.findNamedColor(todo.getColore()).getName() + "</html>"));

        if (todo.getImmagine() != null) {
            ImageIcon imageIcon = new ImageIcon(todo.getImmagine());
            Image image = imageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            infoPanel.add(new JLabel(new ImageIcon(image)));
        }

        return infoPanel;
    }

    private JPanel createToDoDetailButtonPanel(ToDo todo, JDialog detailDialog) {
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

        JButton toggleStatusButton = new JButton(Boolean.TRUE.equals(todo.getStato()) ? "Segna Incompleto" : "Segna Completato");
        toggleStatusButton.addActionListener(e -> {
            detailDialog.dispose();
            controller.toggleToDoStatus(selectedBoardTitle, todo, !Boolean.TRUE.equals(todo.getStato()));
        });

        JButton closeButton = new JButton("Chiudi");
        closeButton.addActionListener(e -> detailDialog.dispose());

        buttonPanel.add(modifyButton);
        buttonPanel.add(moveButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(shareButton);
        buttonPanel.add(toggleStatusButton);
        buttonPanel.add(closeButton);

        return buttonPanel;
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
        final byte[][] immagineSelezionata = {null};

        JPanel panel = createAddToDoPanel(titoloField, descrizioneField, dataScadenzaField, urlField, posizioneField, colorSelector, immagineSelezionata);

        int result = JOptionPane.showConfirmDialog(this, panel, "Nuovo ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if (controller.getUtenteCorrente() != null) {
                LocalDate scadenza = parseDateInput(dataScadenzaField.getText(), LocalDate.now().plusDays(7), true);

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
                JOptionPane.showMessageDialog(this, "Errore: Assicurati di essere loggato.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createAddToDoPanel(JTextField titoloField, JTextField descrizioneField, JTextField dataScadenzaField, JTextField urlField, JTextField posizioneField, JComboBox<NamedColor> colorSelector, byte[][] immagineSelezionata) {
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

        JButton allegaImmagineButton = new JButton("Allega Immagine");
        allegaImmagineButton.addActionListener(ev -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    immagineSelezionata[0] = Files.readAllBytes(file.toPath());
                    JOptionPane.showMessageDialog(this, "Immagine allegata con successo!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Errore nella lettura dell'immagine.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(allegaImmagineButton);

        colorSelector.setSelectedIndex(0);
        JPanel colorPreviewPanel = new JPanel();
        colorPreviewPanel.setBackground(predefinedColors[0].getColor());
        colorPreviewPanel.setPreferredSize(new Dimension(20, 20));
        colorSelector.addActionListener(ev -> {
            NamedColor selected = (NamedColor) colorSelector.getSelectedItem();
            if (selected != null) {
                colorPreviewPanel.setBackground(selected.getColor());
            }
        });
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanel.add(new JLabel("Colore:"));
        colorPanel.add(colorSelector);
        colorPanel.add(colorPreviewPanel);
        panel.add(colorPanel);

        return panel;
    }


    private LocalDate parseDateInput(String dateInput, LocalDate defaultDate, boolean isNew) {
        String trimmedInput = (dateInput != null) ? dateInput.trim() : "";
        if (!trimmedInput.isEmpty()) {
            try {
                return LocalDate.parse(trimmedInput, dateFormatter);
            } catch (DateTimeParseException _) {
                String message = "Formato data non valido. Usare gg/mm/aaaa.\n" +
                        (isNew ? "Verrà usata una data predefinita." : "La data di scadenza non sarà modificata.");
                JOptionPane.showMessageDialog(this, message, "Errore Data", JOptionPane.ERROR_MESSAGE);
                return defaultDate;
            }
        } else {
            String message;
            if (isNew) {
                message = "Data di scadenza non inserita. Verrà usata una data predefinita.";
            } else if (defaultDate != null) {
                message = "Il campo data è stato lasciato vuoto. La data di scadenza è stata rimossa.";
            } else {
                message = null;
            }

            if (message != null) {
                JOptionPane.showMessageDialog(this, message, "Info Data", JOptionPane.INFORMATION_MESSAGE);
            }
            return isNew ? defaultDate : null;
        }
    }


    private void modificaToDo(ToDo toDoDaModificare) {
        JTextField titoloField = new JTextField(toDoDaModificare.getTitolo());
        JTextField descrizioneField = new JTextField(toDoDaModificare.getDescrizione());
        JTextField dataScadenzaField = new JTextField(toDoDaModificare.getScadenza() != null ? toDoDaModificare.getScadenza().format(dateFormatter) : "");
        JTextField urlField = new JTextField(toDoDaModificare.getUrl());
        JTextField posizioneField = new JTextField(toDoDaModificare.getPosizione());
        JCheckBox completatoCheckBoxDialog = new JCheckBox("Completato", Boolean.TRUE.equals(toDoDaModificare.getStato()));
        JComboBox<NamedColor> colorSelectorModify = new JComboBox<>(predefinedColors);
        final byte[][] nuovaImmagine = {toDoDaModificare.getImmagine()};

        JPanel panel = createModifyToDoPanel(toDoDaModificare, titoloField, descrizioneField, dataScadenzaField, urlField, posizioneField, completatoCheckBoxDialog, colorSelectorModify, nuovaImmagine);

        int result = JOptionPane.showConfirmDialog(this, panel, "Modifica ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String nuovoTitolo = titoloField.getText();
            String nuovaDescrizione = descrizioneField.getText();
            String nuovoUrl = urlField.getText().trim();
            String nuovaPosizione = posizioneField.getText().trim();
            boolean nuovoStato = completatoCheckBoxDialog.isSelected();
            NamedColor namedColorSelected = (NamedColor) colorSelectorModify.getSelectedItem();
            Color nuovoColore = (namedColorSelected != null) ? namedColorSelected.getColor() : Color.WHITE;

            LocalDate nuovaScadenza = parseDateInput(dataScadenzaField.getText(), toDoDaModificare.getScadenza(), false);

            controller.modificaToDo(selectedBoardTitle, toDoDaModificare, nuovoTitolo, nuovaDescrizione, nuovaScadenza, nuovoStato, nuovoColore, nuovoUrl, nuovaPosizione, toDoDaModificare.getImmagine());
        }
    }

    private JPanel createModifyToDoPanel(ToDo toDoDaModificare, JTextField titoloField, JTextField descrizioneField, JTextField dataScadenzaField, JTextField urlField, JTextField posizioneField, JCheckBox completatoCheckBoxDialog, JComboBox<NamedColor> colorSelectorModify, byte[][] nuovaImmagine) {
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

        JButton attachImageButton = new JButton("Allega Immagine");
        attachImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    nuovaImmagine[0] = Files.readAllBytes(selectedFile.toPath());
                    JOptionPane.showMessageDialog(this, "Immagine allegata con successo.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Errore durante la lettura dell'immagine.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(attachImageButton);

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
        JPanel colorPanelModify = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanelModify.add(new JLabel("Colore:"));
        colorPanelModify.add(colorSelectorModify);
        colorPanelModify.add(colorPreviewPanelModify);
        panel.add(colorPanelModify);

        return panel;
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

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Seleziona i contatti o inserisci una nuova email:"), BorderLayout.NORTH);

        // Pannello per i contatti esistenti
        JPanel contattiPanel = new JPanel(new GridLayout(0, 1));
        List<JCheckBox> checkBoxes = new ArrayList<>();
        if (contatti != null && !contatti.getLista().isEmpty()) {
            for (String email : contatti.getLista()) {
                JCheckBox checkBox = new JCheckBox(email);
                checkBoxes.add(checkBox);
                contattiPanel.add(checkBox);
            }
        } else {
            contattiPanel.add(new JLabel("Nessun contatto disponibile."));
        }

        // Pannello per l'inserimento di una nuova email
        JPanel emailInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        emailInputPanel.setBorder(BorderFactory.createTitledBorder("Condividi con una nuova email"));
        JTextField emailField = new JTextField(25);
        emailInputPanel.add(new JLabel("Email:"));
        emailInputPanel.add(emailField);

        panel.add(new JScrollPane(contattiPanel), BorderLayout.CENTER);
        panel.add(emailInputPanel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel, "Condividi ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Condivisione con i contatti selezionati
            for (JCheckBox checkBox : checkBoxes) {
                if (checkBox.isSelected()) {
                    controller.condividiToDo(todo, checkBox.getText());
                }
            }

            // Condivisione con la nuova email inserita
            String nuovaEmail = emailField.getText().trim();
            if (!nuovaEmail.isEmpty()) {
                controller.condividiToDo(todo, nuovaEmail);
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
            JOptionPane.showMessageDialog(this, "Errore: Bacheca non trovata.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        String descrizioneAttuale = bachecaCorrente.getDescrizione();

        Object input = JOptionPane.showInputDialog(
                this,
                "Modifica descrizione per la bacheca '" + selectedBoardTitle.name() + "':",
                MODIFY_BOARD_DESC_TITLE,
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
        LOGGER.log(Level.INFO, "Contact list refreshed.");
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
        private static final DateTimeFormatter cellDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        public ToDoCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ToDo> list, ToDo todo, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (todo != null) {
                setSelected(Boolean.TRUE.equals(todo.getStato()));
                String scadenzaStr = (todo.getScadenza() != null) ? todo.getScadenza().format(cellDateFormatter) : "N/D";
                setText(String.format("%s (Scad: %s) - %s",
                        todo.getTitolo(), scadenzaStr, todo.getDescrizione()));

                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    Color bgColor = todo.getColore() != null ? todo.getColore() : list.getBackground();
                    setBackground(bgColor);
                    if (bgColor != null && isColorDark(bgColor) && !bgColor.equals(Color.WHITE)) {
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

        private static boolean isColorDark(Color color) {
            if (color == null) return false;
            double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
            return luminance < 0.5;
        }
    }
}