package gui;

import controller.Controller;
import org.ToDo.*;

import javax.swing.*;
import java.awt.*;
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
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * La classe View rappresenta la finestra principale dell'interfaccia grafica (GUI) dell'applicazione.
 * Estende {@link JFrame} e si occupa di costruire e gestire tutti i componenti visivi,
 * come pannelli, pulsanti e liste. Interagisce con il {@link Controller} per
 * gestire gli eventi dell'utente e aggiornare i dati visualizzati.
 */
public class View extends JFrame {
    /** Controller dell'applicazione per la gestione della logica di business. */
    private final transient Controller controller;
    /** Pannello contenitore per la selezione delle bacheche. */
    private final JPanel boardSelectionContainer;
    /** Pannello principale dove viene visualizzata la lista dei ToDo o i riquadri. */
    private final JPanel todoListPanel;
    /** Pannello divisore che separa la selezione bacheche dalla lista ToDo. */
    private final JSplitPane mainSplitPane;
    /** Checkbox per filtrare la visualizzazione dei ToDo completati. */
    private final JCheckBox showCompletedCheckBox;
    /** Modello dati per la JList dei ToDo. */
    private DefaultListModel<ToDo> todoListModel;
    /** Lista visuale per i ToDo. */
    private JList<ToDo> todoList;
    /** Etichetta che mostra la descrizione della bacheca correntemente selezionata. */
    private JLabel currentBoardDescriptionLabel;
    /** Titolo della bacheca attualmente selezionata. */
    private Titolo selectedBoardTitle;
    /** Formattatore per le date nel formato "dd/MM/yyyy". */
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    /** Array di colori predefiniti tra cui l'utente può scegliere. */
    private final transient NamedColor[] predefinedColors = NamedColor.getPredefinedColors();
    /** Flag per indicare se la vista corrente è a riquadri (true) o a lista (false). */
    private boolean isTileView = false;
    /** Pulsante per alternare tra la vista a lista e la vista a riquadri. */
    private final JToggleButton viewToggleButton;

    /** Logger per la registrazione di eventi e errori della View. */
    private static final Logger LOGGER = Logger.getLogger(View.class.getName());
    /** Titolo standard per le finestre di dialogo di errore. */
    private static final String ERROR_TITLE = "Errore";
    /** Titolo per la finestra di dialogo di modifica della descrizione della bacheca. */
    private static final String MODIFY_BOARD_DESC_TITLE = "Modifica Descrizione Bacheca";
    /** Testo per il pulsante di aggiunta di un nuovo ToDo. */
    private static final String ADD_TODO_BUTTON_TEXT = "<html><span style='font-size:1.2em;'>&#43;</span> Aggiungi ToDo</html>";
    /** Testo per il checkbox che mostra i ToDo completati. */
    private static final String SHOW_COMPLETED_CHECKBOX_TEXT = "Mostra ToDo Completati";
    /** Testo per il pulsante di modifica della descrizione della bacheca. */
    private static final String MODIFY_DESCRIPTION_BUTTON_TEXT = "Modifica Descrizione";
    /** Nome del font utilizzato in alcuni componenti. */
    private static final String FONT_NAME = "Arial";
    /** Tag di apertura HTML per il grassetto. */
    private static final String HTML_BOLD_START = "<html><b>";
    /** Tag di chiusura HTML. */
    private static final String HTML_END = "</b></html>";

    /**
     * Classe interna statica per raggruppare i componenti del form di aggiunta/modifica ToDo.
     * Questo migliora l'organizzazione e la leggibilità del codice, incapsulando tutti gli
     * elementi di un form in un unico oggetto.
     */
    private static class ToDoFormElements {
        JPanel panel;
        JTextField titoloField;
        JTextField descrizioneField;
        JTextField dataScadenzaField;
        JTextField urlField;
        JTextField posizioneField;
        JComboBox<NamedColor> colorSelector;
        JCheckBox completatoCheckBox; // Può essere null per il dialogo di aggiunta
        final byte[][] immagineSelezionata = {null};
    }

    /**
     * Costruttore della View. Inizializza la finestra principale e i suoi componenti.
     *
     * @param controller l'istanza del {@link Controller} che gestirà la logica dell'applicazione.
     */
    public View(Controller controller) {
        this.controller = controller;
        setTitle("Gestione ToDo - Utente: " + (controller.getUtenteCorrente() != null ? controller.getUtenteCorrente().getEmail() : "N/A"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());

        showCompletedCheckBox = new JCheckBox(SHOW_COMPLETED_CHECKBOX_TEXT);
        showCompletedCheckBox.addActionListener(_ -> refreshToDoList());

        viewToggleButton = new JToggleButton("Vista Riquadri");
        viewToggleButton.setFocusPainted(false);
        viewToggleButton.addActionListener(_ -> {
            isTileView = viewToggleButton.isSelected();
            refreshToDoList();
        });

        JPanel topNavBarPanel = createTopNavBar();
        this.add(topNavBarPanel, BorderLayout.NORTH);

        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerSize(0);
        mainSplitPane.setEnabled(false);
        mainSplitPane.setResizeWeight(0.0);

        boardSelectionContainer = createBoardSelectionContainer();
        JPanel mainBoardsPanel = new JPanel(new GridBagLayout());
        mainBoardsPanel.add(boardSelectionContainer);
        mainSplitPane.setLeftComponent(mainBoardsPanel);

        todoListPanel = new JPanel(new BorderLayout());
        todoListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainSplitPane.setRightComponent(todoListPanel);

        this.add(mainSplitPane, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> mainSplitPane.setDividerLocation(1.0));
    }

    /**
     * Crea e restituisce la barra di navigazione superiore dell'applicazione.
     * Contiene il titolo, i pulsanti per le azioni principali (Home, Profilo, etc.) e il logout.
     *
     * @return un {@link JPanel} che rappresenta la barra di navigazione.
     */
    private JPanel createTopNavBar() {
        JPanel topNavBarPanel = new JPanel(new BorderLayout());
        topNavBarPanel.setBackground(new Color(230, 230, 230));
        topNavBarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JLabel appNameLabel = new JLabel("  ToDo App");
        appNameLabel.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        topNavBarPanel.add(appNameLabel, BorderLayout.WEST);

        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        buttonContainer.setOpaque(false);

        JButton homeButton = new JButton("Home");
        homeButton.setFocusPainted(false);
        homeButton.addActionListener(_ -> showBoardSelectionView());
        buttonContainer.add(homeButton);

        JButton profileButton = new JButton("Profilo");
        profileButton.setFocusPainted(false);
        profileButton.addActionListener(_ -> showProfileDialog());
        buttonContainer.add(profileButton);

        JButton sharedButton = new JButton("Condivisi");
        sharedButton.setFocusPainted(false);
        sharedButton.addActionListener(_ -> showSharedToDoList());
        buttonContainer.add(sharedButton);

        JButton allIncompleteToDosButton = new JButton("ToDo");
        allIncompleteToDosButton.setFocusPainted(false);
        allIncompleteToDosButton.addActionListener(_ -> showAllIncompleteToDos());
        buttonContainer.add(allIncompleteToDosButton);

        JButton contactsButton = new JButton("Contatti");
        contactsButton.setFocusPainted(false);
        contactsButton.addActionListener(_ -> showContactsDialog());
        buttonContainer.add(contactsButton);

        buttonContainer.add(viewToggleButton);

        JButton logoutButtonTop = new JButton("Logout");
        logoutButtonTop.setForeground(Color.RED);
        logoutButtonTop.setFocusPainted(false);
        logoutButtonTop.addActionListener(this::performLogout);
        buttonContainer.add(logoutButtonTop);

        topNavBarPanel.add(buttonContainer, BorderLayout.EAST);
        return topNavBarPanel;
    }

    /**
     * Crea il contenitore per la selezione delle bacheche, mostrato nella vista iniziale.
     *
     * @return un {@link JPanel} che contiene i pannelli delle singole bacheche.
     */
    private JPanel createBoardSelectionContainer() {
        JPanel container = new JPanel(new GridLayout(1, 3, 20, 0));
        container.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        container.setBackground(new Color(240, 240, 240));
        for (Titolo title : Titolo.values()) {
            container.add(createBoardPanel(title));
        }
        return container;
    }

    /**
     * Crea un pannello cliccabile che rappresenta una singola bacheca.
     *
     * @param title il {@link Titolo} della bacheca da rappresentare.
     * @return un {@link JPanel} per la bacheca.
     */
    private JPanel createBoardPanel(Titolo title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setPreferredSize(new Dimension(200, 150));
        JLabel titleLabel = new JLabel(title.name().replace("_", " "), SwingConstants.CENTER);
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent unused) {
                selectedBoardTitle = title;
                showToDoListForBoard();
            }
        });
        return panel;
    }

    /**
     * Crea e configura una JList per visualizzare i ToDo.
     *
     * @param mouseClickConsumer un'azione da eseguire quando viene fatto clic su un elemento della lista.
     * @return una JList<ToDo> configurata.
     */
    private JList<ToDo> createToDoJList(Consumer<MouseEvent> mouseClickConsumer) {
        todoListModel = new DefaultListModel<>();
        JList<ToDo> list = new JList<>(todoListModel);
        list.setCellRenderer(new ToDoCellRenderer());
        list.setFixedCellHeight(30);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseClickConsumer.accept(e);
            }
        });
        return list;
    }

    /**
     * Processa un click del mouse su una JList di ToDo.
     *
     * @param list la JList su cui è avvenuto il click.
     * @param point il punto in cui è avvenuto il click.
     * @param action l'azione da eseguire con il ToDo selezionato.
     */
    private void processToDoClick(JList<ToDo> list, Point point, Consumer<ToDo> action) {
        int index = list.locationToIndex(point);
        if (index != -1) {
            action.accept(list.getModel().getElementAt(index));
        }
    }

    /**
     * Mostra la lista dei ToDo che sono stati condivisi con l'utente corrente.
     * Imposta la vista per visualizzare questi ToDo in un pannello dedicato.
     */
    @SuppressWarnings("unchecked")
    private void showSharedToDoList() {
        selectedBoardTitle = null;
        boardSelectionContainer.setLayout(new GridLayout(0, 1, 0, 20));
        mainSplitPane.setDividerLocation(0.35);
        todoListPanel.removeAll();
        todoListPanel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        currentBoardDescriptionLabel = new JLabel("Descrizione: ToDo condivisi da altri utenti");
        headerPanel.add(currentBoardDescriptionLabel, BorderLayout.NORTH);
        todoListPanel.add(headerPanel, BorderLayout.NORTH);

        todoList = createToDoJList(e -> processToDoClick((JList<ToDo>) e.getSource(), e.getPoint(), this::showToDoDetailDialog));

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
     * Mostra la lista dei ToDo per la bacheca correntemente selezionata.
     * Configura la JList per gestire i click sull'area del checkbox o sull'elemento stesso.
     */
    @SuppressWarnings("unchecked")
    private void showToDoListForBoard() {
        boardSelectionContainer.setLayout(new GridLayout(0, 1, 0, 20));
        mainSplitPane.setDividerLocation(0.35);

        todoList = createToDoJList(e -> processToDoClick((JList<ToDo>) e.getSource(), e.getPoint(), todo -> {
            if (e.getX() < 25) { // Click sull'area del checkbox
                controller.toggleToDoStatus(selectedBoardTitle, todo, !Boolean.TRUE.equals(todo.getStato()));
            } else {
                showToDoDetailDialog(todo);
            }
        }));

        refreshToDoList();
    }

    /**
     * Mostra una vista speciale con tutti i ToDo non completati di tutte le bacheche dell'utente.
     */
    @SuppressWarnings("unchecked")
    private void showAllIncompleteToDos() {
        selectedBoardTitle = null;
        boardSelectionContainer.setLayout(new GridLayout(0, 1, 0, 20));
        mainSplitPane.setDividerLocation(0.35);

        todoList = createToDoJList(e -> processToDoClick((JList<ToDo>) e.getSource(), e.getPoint(), this::showToDoDetailDialog));

        refreshToDoList();
    }

    /**
     * Aggiorna e ricarica la visualizzazione della lista dei ToDo.
     * Questo metodo viene chiamato ogni volta che i dati cambiano (es. aggiunta, modifica, cambio vista).
     * Ricostruisce l'header, il corpo (lista o riquadri) e il footer del pannello dei ToDo.
     */
    public void refreshToDoList() {
        todoListPanel.removeAll();
        todoListPanel.setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        todoListPanel.add(headerPanel, BorderLayout.NORTH);

        if (isTileView) {
            populateTileView();
        } else {
            populateListView();
        }

        JPanel buttonPanel = createFooterPanel();
        todoListPanel.add(buttonPanel, BorderLayout.SOUTH);

        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    /**
     * Crea il pannello di intestazione per la vista della lista dei ToDo.
     * Include la descrizione della bacheca e, se applicabile, il pulsante per modificarla e il checkbox per i ToDo completati.
     *
     * @return un {@link JPanel} che funge da header.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        String descriptionText;
        if (selectedBoardTitle == null) {
            descriptionText = "Descrizione: Tutti i ToDo Incompiuti";
        } else {
            descriptionText = "Descrizione: " + controller.getBacheche().get(selectedBoardTitle).getDescrizione();
        }
        currentBoardDescriptionLabel = new JLabel(descriptionText);

        JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        descriptionPanel.add(currentBoardDescriptionLabel);

        if (selectedBoardTitle != null) {
            JButton modifyBoardDescriptionButton = new JButton(MODIFY_DESCRIPTION_BUTTON_TEXT);
            modifyBoardDescriptionButton.addActionListener(this::modificaDescrizioneBachecaSelezionata);
            descriptionPanel.add(modifyBoardDescriptionButton);
            headerPanel.add(showCompletedCheckBox, BorderLayout.SOUTH);
        }

        headerPanel.add(descriptionPanel, BorderLayout.NORTH);
        return headerPanel;
    }

    /**
     * Crea il pannello a piè di pagina per la vista dei ToDo.
     * Contiene il pulsante per aggiungere un nuovo ToDo.
     *
     * @return un {@link JPanel} che funge da footer.
     */
    private JPanel createFooterPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addToDoButton = new JButton(ADD_TODO_BUTTON_TEXT);
        if (selectedBoardTitle == null) {
            addToDoButton.addActionListener(_ -> aggiungiToDoGlobale());
        } else {
            addToDoButton.addActionListener(this::aggiungiToDo);
        }
        buttonPanel.add(addToDoButton);
        return buttonPanel;
    }

    /**
     * Popola il pannello dei ToDo con una vista a riquadri (tile view).
     * Ogni ToDo viene mostrato come una "card" separata.
     */
    private void populateTileView() {
        JPanel cardsContainer = new JPanel(new GridLayout(0, 2, 10, 10));
        cardsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (selectedBoardTitle == null) {
            controller.getBacheche().values().stream()
                    .flatMap(bacheca -> bacheca.getToDos().stream())
                    .filter(t -> !Boolean.TRUE.equals(t.getStato()))
                    .forEach(t -> cardsContainer.add(createToDoCard(t)));
        } else {
            Bacheca bacheca = controller.getBacheche().get(selectedBoardTitle);
            if (bacheca != null) {
                bacheca.getToDos().stream()
                        .filter(t -> showCompletedCheckBox.isSelected() || !Boolean.TRUE.equals(t.getStato()))
                        .forEach(t -> cardsContainer.add(createToDoCard(t)));
            }
        }
        todoListPanel.add(new JScrollPane(cardsContainer), BorderLayout.CENTER);
    }

    /**
     * Popola il pannello dei ToDo con una vista a lista tradizionale.
     * Utilizza il DefaultListModel per aggiungere gli elementi.
     */
    private void populateListView() {
        if (todoListModel == null) todoListModel = new DefaultListModel<>();
        todoListModel.clear();

        if (selectedBoardTitle == null) {
            controller.getBacheche().values().stream()
                    .flatMap(bacheca -> bacheca.getToDos().stream())
                    .filter(t -> !Boolean.TRUE.equals(t.getStato()))
                    .forEach(todoListModel::addElement);
        } else {
            Bacheca bacheca = controller.getBacheche().get(selectedBoardTitle);
            if (bacheca != null) {
                bacheca.getToDos().stream()
                        .filter(t -> showCompletedCheckBox.isSelected() || !Boolean.TRUE.equals(t.getStato()))
                        .forEach(todoListModel::addElement);
            }
        }

        if (todoList == null) todoList = new JList<>();
        todoList.setModel(todoListModel);
        todoListPanel.add(new JScrollPane(todoList), BorderLayout.CENTER);
    }

    /**
     * Gestisce l'aggiunta di un ToDo quando non è selezionata una bacheca specifica.
     * Chiede all'utente di selezionare una bacheca di destinazione.
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
     * Ripristina la vista iniziale mostrando la selezione delle bacheche.
     */
    private void showBoardSelectionView() {
        selectedBoardTitle = null;
        boardSelectionContainer.setLayout(new GridLayout(1, 4, 20, 0));
        mainSplitPane.setDividerLocation(1.0);
    }

    /**
     * Crea un pannello "card" per rappresentare un singolo ToDo nella vista a riquadri.
     * La card viene colorata di rosso chiaro se il ToDo è scaduto e non completato.
     *
     * @param todo il ToDo da rappresentare.
     * @return un {@link JPanel} che rappresenta la card del ToDo.
     */
    private JPanel createToDoCard(ToDo todo) {
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        cardPanel.setPreferredSize(new Dimension(180, 120));

        // Controlla se il ToDo è scaduto e non completato
        if (todo.getScadenza() != null && todo.getScadenza().isBefore(LocalDate.now()) && !Boolean.TRUE.equals(todo.getStato())) {
            cardPanel.setBackground(new Color(255, 182, 193)); // Rosso chiaro per evidenziare
        } else {
            cardPanel.setBackground(todo.getColore() != null ? todo.getColore() : Color.WHITE);
        }

        JLabel titleLabel = new JLabel(HTML_BOLD_START + todo.getTitolo() + HTML_END);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 14));

        JLabel descLabel = new JLabel("<html>" + todo.getDescrizione() + "</html>");
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 12));

        Color bgColor = cardPanel.getBackground(); // Usa il colore di sfondo già impostato
        if (ToDoCellRenderer.isColorDark(bgColor)) {
            titleLabel.setForeground(Color.WHITE);
            descLabel.setForeground(Color.WHITE);
        } else {
            titleLabel.setForeground(Color.BLACK);
            descLabel.setForeground(Color.BLACK);
        }

        cardPanel.add(titleLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        cardPanel.add(descLabel);

        cardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent unused) {
                showToDoDetailDialog(todo);
            }
        });
        return cardPanel;
    }

    /**
     * Mostra una finestra di dialogo con i dettagli completi di un ToDo.
     *
     * @param todo il ToDo di cui mostrare i dettagli.
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

    /**
     * Crea il pannello informativo per la finestra di dialogo dei dettagli del ToDo.
     *
     * @param todo il ToDo da visualizzare.
     * @param parentDialog il dialogo genitore.
     * @return un {@link JPanel} con le informazioni del ToDo.
     */
    private JPanel createToDoDetailInfoPanel(ToDo todo, JDialog parentDialog) {
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        infoPanel.add(new JLabel(HTML_BOLD_START + "Titolo:</b> " + todo.getTitolo() + HTML_END));
        infoPanel.add(new JLabel(HTML_BOLD_START + "Descrizione:</b> " + todo.getDescrizione() + HTML_END));
        infoPanel.add(new JLabel(HTML_BOLD_START + "Autore:</b> " + todo.getListaUtenti().getAutore() + HTML_END));
        infoPanel.add(new JLabel(HTML_BOLD_START + "Condiviso con:</b> " + (todo.getListaUtenti().getLista().isEmpty() ? "Nessuno" : String.join(", ", todo.getListaUtenti().getLista())) + HTML_END));
        infoPanel.add(new JLabel(HTML_BOLD_START + "Stato:</b> " + (Boolean.TRUE.equals(todo.getStato()) ? "Completato" : "Incompleto") + HTML_END));
        infoPanel.add(new JLabel(HTML_BOLD_START + "Scadenza:</b> " + (todo.getScadenza() != null ? todo.getScadenza().format(dateFormatter) : "N/D") + HTML_END));

        if (todo.getUrl() != null && !todo.getUrl().isEmpty()) {
            infoPanel.add(createUrlLabel(todo, parentDialog));
        } else {
            infoPanel.add(new JLabel(HTML_BOLD_START + "URL:</b> N/D" + HTML_END));
        }

        infoPanel.add(new JLabel(HTML_BOLD_START + "Posizione:</b> " + (todo.getPosizione() != null && !todo.getPosizione().isEmpty() ? todo.getPosizione().trim() : "N/D") + HTML_END));

        String hexColor = String.format("#%06x", todo.getColore().getRGB() & 0xFFFFFF);
        infoPanel.add(new JLabel("<html><b>Colore:</b> <font color='" + hexColor + "'>&#9632;</font> " + NamedColor.findNamedColor(todo.getColore()).name() + "</html>"));

        if (todo.getImmagine() != null) {
            ImageIcon imageIcon = new ImageIcon(todo.getImmagine());
            Image image = imageIcon.getImage().getScaledInstance(150, -1, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showFullImageDialog(imageIcon);
                }
            });
            infoPanel.add(imageLabel);
        }

        return infoPanel;
    }

    /**
     * Mostra un'immagine a grandezza naturale in una nuova finestra di dialogo.
     *
     * @param imageIcon l'icona dell'immagine da visualizzare.
     */
    private void showFullImageDialog(ImageIcon imageIcon) {
        JDialog imageDialog = new JDialog(this, "Immagine ToDo", true);
        JLabel fullImageLabel = new JLabel(imageIcon);
        imageDialog.add(new JScrollPane(fullImageLabel));
        imageDialog.pack();
        imageDialog.setLocationRelativeTo(this);
        imageDialog.setVisible(true);
    }

    /**
     * Crea una JLabel cliccabile per un URL, che apre il link nel browser predefinito.
     *
     * @param todo il ToDo contenente l'URL.
     * @param parent il dialogo genitore.
     * @return una {@link JLabel} cliccabile.
     */
    private JLabel createUrlLabel(ToDo todo, JDialog parent) {
        JLabel urlLabel = new JLabel("<html><b>URL:</b> <a href='" + todo.getUrl() + "'>" + todo.getUrl() + "</a></html>");
        urlLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        urlLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent unused) {
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(todo.getUrl()));
                    }
                } catch (URISyntaxException | IOException _) {
                    JOptionPane.showMessageDialog(parent, "Errore nell'apertura dell'URL.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return urlLabel;
    }

    /**
     * Crea il pannello dei pulsanti per la finestra di dialogo dei dettagli del ToDo.
     *
     * @param todo il ToDo a cui si riferiscono i pulsanti.
     * @param detailDialog il dialogo che contiene i pulsanti.
     * @return un {@link JPanel} con i pulsanti di azione.
     */
    private JPanel createToDoDetailButtonPanel(ToDo todo, JDialog detailDialog) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton modifyButton = new JButton("Modifica");
        modifyButton.addActionListener(_ -> { detailDialog.dispose(); modificaToDo(todo); });
        buttonPanel.add(modifyButton);

        JButton moveButton = new JButton("Sposta");
        moveButton.addActionListener(_ -> { detailDialog.dispose(); spostaToDo(todo); });
        buttonPanel.add(moveButton);

        JButton deleteButton = new JButton("Elimina");
        deleteButton.addActionListener(_ -> { detailDialog.dispose(); rimuoviToDo(todo); });
        buttonPanel.add(deleteButton);

        JButton shareButton = new JButton("Condividi");
        shareButton.addActionListener(_ -> { detailDialog.dispose(); condividiToDo(todo); });
        buttonPanel.add(shareButton);

        String toggleText = Boolean.TRUE.equals(todo.getStato()) ? "Segna Incompleto" : "Segna Completato";
        JButton toggleStatusButton = new JButton(toggleText);
        toggleStatusButton.addActionListener(_ -> {
            detailDialog.dispose();
            controller.toggleToDoStatus(selectedBoardTitle, todo, !Boolean.TRUE.equals(todo.getStato()));
        });
        buttonPanel.add(toggleStatusButton);

        JButton closeButton = new JButton("Chiudi");
        closeButton.addActionListener(_ -> detailDialog.dispose());
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    /**
     * Gestisce l'evento di click sul pulsante "Aggiungi ToDo".
     * Mostra un dialogo per inserire i dati del nuovo ToDo.
     *
     * @param unused l'evento di azione (non utilizzato).
     */
    private void aggiungiToDo(java.awt.event.ActionEvent unused) {
        if (selectedBoardTitle == null) {
            JOptionPane.showMessageDialog(this, "Seleziona una bacheca prima di aggiungere un ToDo.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ToDoFormElements form = createAddToDoPanel();

        int result = JOptionPane.showConfirmDialog(this, form.panel, "Nuovo ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if (controller.getUtenteCorrente() != null) {
                LocalDate scadenza = parseDateInput(form.dataScadenzaField.getText(), LocalDate.now().plusDays(7), true);
                NamedColor namedColorSelected = (NamedColor) form.colorSelector.getSelectedItem();
                Color coloreScelto = (namedColorSelected != null) ? namedColorSelected.color() : Color.WHITE;

                ToDo nuovo = new ToDo(
                        form.titoloField.getText(),
                        form.descrizioneField.getText(),
                        scadenza,
                        false,
                        form.urlField.getText().trim(),
                        form.posizioneField.getText().trim(),
                        coloreScelto,
                        form.immagineSelezionata[0],
                        selectedBoardTitle,
                        controller.getUtenteCorrente().getEmail()
                );
                controller.aggiungiToDo(selectedBoardTitle, nuovo);
            } else {
                JOptionPane.showMessageDialog(this, "Errore: Assicurati di essere loggato.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Crea e restituisce un oggetto ToDoFormElements per il dialogo di aggiunta di un nuovo ToDo.
     *
     * @return un oggetto {@link ToDoFormElements} con tutti i campi del form.
     */
    private ToDoFormElements createAddToDoPanel() {
        ToDoFormElements form = new ToDoFormElements();
        form.panel = new JPanel(new GridLayout(0, 1));
        form.titoloField = new JTextField();
        form.descrizioneField = new JTextField();
        form.dataScadenzaField = new JTextField(10);
        form.urlField = new JTextField();
        form.posizioneField = new JTextField();
        form.colorSelector = new JComboBox<>(predefinedColors);

        form.panel.add(new JLabel("Titolo:"));
        form.panel.add(form.titoloField);
        form.panel.add(new JLabel("Descrizione:"));
        form.panel.add(form.descrizioneField);
        form.panel.add(new JLabel("Data Scadenza (gg/mm/aaaa):"));
        form.panel.add(form.dataScadenzaField);
        form.panel.add(new JLabel("URL (opzionale):"));
        form.panel.add(form.urlField);
        form.panel.add(new JLabel("Posizione (opzionale):"));
        form.panel.add(form.posizioneField);

        JButton allegaImmagineButton = new JButton("Allega Immagine");
        allegaImmagineButton.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    form.immagineSelezionata[0] = Files.readAllBytes(file.toPath());
                    JOptionPane.showMessageDialog(this, "Immagine allegata con successo!");
                } catch (IOException _) {
                    JOptionPane.showMessageDialog(this, "Errore nella lettura dell'immagine.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        form.panel.add(allegaImmagineButton);

        form.colorSelector.setSelectedIndex(0);
        JPanel colorPreviewPanel = new JPanel();
        colorPreviewPanel.setBackground(predefinedColors[0].color());
        colorPreviewPanel.setPreferredSize(new Dimension(20, 20));
        form.colorSelector.addActionListener(_ -> {
            NamedColor selected = (NamedColor) form.colorSelector.getSelectedItem();
            if (selected != null) {
                colorPreviewPanel.setBackground(selected.color());
            }
        });
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanel.add(new JLabel("Colore:"));
        colorPanel.add(form.colorSelector);
        colorPanel.add(colorPreviewPanel);
        form.panel.add(colorPanel);

        return form;
    }

    /**
     * Esegue il parsing di una stringa di data e la converte in un oggetto LocalDate.
     * Gestisce input vuoti, formati non validi e impedisce l'inserimento di date passate.
     *
     * @param dateInput la stringa della data da parsare (formato "dd/MM/yyyy").
     * @param defaultDate la data da usare se l'input è vuoto o non valido.
     * @param isNew indica se il ToDo è nuovo (per personalizzare il messaggio di errore).
     * @return un oggetto {@link LocalDate} valido o la data di default.
     */
    private LocalDate parseDateInput(String dateInput, LocalDate defaultDate, boolean isNew) {
        String trimmedInput = (dateInput != null) ? dateInput.trim() : "";
        if (!trimmedInput.isEmpty()) {
            try {
                LocalDate parsedDate = LocalDate.parse(trimmedInput, dateFormatter);
                if (parsedDate.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "Non è possibile inserire date antecedenti all'odierna come scadenza.", "Data non valida", JOptionPane.ERROR_MESSAGE);
                    return defaultDate; // Ritorna la data di default o gestisci come preferisci
                }
                return parsedDate;
            } catch (DateTimeParseException _) {
                String message = "Formato data non valido. Usare gg/mm/aaaa.\n" + (isNew ? "Verrà usata una data predefinita." : "La data di scadenza non sarà modificata.");
                JOptionPane.showMessageDialog(this, message, "Errore Data", JOptionPane.ERROR_MESSAGE);
                return defaultDate;
            }
        } else {
            return isNew ? defaultDate : null;
        }
    }

    /**
     * Apre una finestra di dialogo per modificare un ToDo esistente.
     *
     * @param toDoDaModificare il ToDo da modificare.
     */
    private void modificaToDo(ToDo toDoDaModificare) {
        ToDoFormElements form = createModifyToDoPanel(toDoDaModificare);

        int result = JOptionPane.showConfirmDialog(this, form.panel, "Modifica ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String nuovoTitolo = form.titoloField.getText();
            String nuovaDescrizione = form.descrizioneField.getText();
            LocalDate nuovaScadenza = parseDateInput(form.dataScadenzaField.getText(), toDoDaModificare.getScadenza(), false);
            boolean nuovoStato = form.completatoCheckBox.isSelected();
            NamedColor namedColorSelected = (NamedColor) form.colorSelector.getSelectedItem();
            Color nuovoColore = namedColorSelected != null ? namedColorSelected.color() : Color.WHITE;
            String nuovoUrl = form.urlField.getText().trim();
            String nuovaPosizione = form.posizioneField.getText().trim();

            controller.modificaToDo(selectedBoardTitle, toDoDaModificare, nuovoTitolo, nuovaDescrizione, nuovaScadenza, nuovoStato, nuovoColore, nuovoUrl, nuovaPosizione, form.immagineSelezionata[0]);
        }
    }

    /**
     * Crea un oggetto ToDoFormElements pre-compilato con i dati di un ToDo esistente.
     *
     * @param toDoDaModificare il ToDo i cui dati verranno usati per riempire il form.
     * @return un oggetto {@link ToDoFormElements} per il form di modifica.
     */
    private ToDoFormElements createModifyToDoPanel(ToDo toDoDaModificare) {
        ToDoFormElements form = new ToDoFormElements();
        form.panel = new JPanel(new GridLayout(0, 1));
        form.titoloField = new JTextField(toDoDaModificare.getTitolo());
        form.descrizioneField = new JTextField(toDoDaModificare.getDescrizione());
        form.dataScadenzaField = new JTextField(toDoDaModificare.getScadenza() != null ? toDoDaModificare.getScadenza().format(dateFormatter) : "");
        form.urlField = new JTextField(toDoDaModificare.getUrl());
        form.posizioneField = new JTextField(toDoDaModificare.getPosizione());
        form.completatoCheckBox = new JCheckBox("Completato", Boolean.TRUE.equals(toDoDaModificare.getStato()));
        form.colorSelector = new JComboBox<>(predefinedColors);
        form.immagineSelezionata[0] = toDoDaModificare.getImmagine();

        form.panel.add(new JLabel("Titolo:"));
        form.panel.add(form.titoloField);
        form.panel.add(new JLabel("Descrizione:"));
        form.panel.add(form.descrizioneField);
        form.panel.add(new JLabel("Data Scadenza (gg/mm/aaaa):"));
        form.panel.add(form.dataScadenzaField);
        form.panel.add(new JLabel("URL (opzionale):"));
        form.panel.add(form.urlField);
        form.panel.add(new JLabel("Posizione (opzionale):"));
        form.panel.add(form.posizioneField);
        form.panel.add(form.completatoCheckBox);

        JButton attachImageButton = new JButton("Allega Immagine");
        attachImageButton.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    form.immagineSelezionata[0] = Files.readAllBytes(selectedFile.toPath());
                    JOptionPane.showMessageDialog(this, "Immagine allegata con successo.");
                } catch (IOException _) {
                    JOptionPane.showMessageDialog(this, "Errore durante la lettura dell'immagine.", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        form.panel.add(attachImageButton);

        NamedColor currentColor = NamedColor.findNamedColor(toDoDaModificare.getColore());
        form.colorSelector.setSelectedItem(currentColor);
        JPanel colorPreviewPanelModify = new JPanel();
        colorPreviewPanelModify.setBackground(currentColor.color());
        colorPreviewPanelModify.setPreferredSize(new Dimension(20, 20));
        form.colorSelector.addActionListener(_ -> {
            NamedColor selected = (NamedColor) form.colorSelector.getSelectedItem();
            if (selected != null) {
                colorPreviewPanelModify.setBackground(selected.color());
            }
        });
        JPanel colorPanelModify = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanelModify.add(new JLabel("Colore:"));
        colorPanelModify.add(form.colorSelector);
        colorPanelModify.add(colorPreviewPanelModify);
        form.panel.add(colorPanelModify);

        return form;
    }

    /**
     * Mostra un dialogo per spostare un ToDo in un'altra bacheca.
     *
     * @param toDoDaSpostare il ToDo da spostare.
     */
    private void spostaToDo(ToDo toDoDaSpostare) {
        if (toDoDaSpostare == null) return;
        ArrayList<Titolo> opzioniDestinazioneList = new ArrayList<>(Arrays.asList(Titolo.values()));
        opzioniDestinazioneList.remove(selectedBoardTitle);
        if (opzioniDestinazioneList.isEmpty()) return;

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

    /**
     * Chiede conferma e poi rimuove un ToDo.
     *
     * @param toDoDaRimuovere il ToDo da rimuovere.
     */
    private void rimuoviToDo(ToDo toDoDaRimuovere) {
        if (toDoDaRimuovere == null) return;
        int choice = JOptionPane.showConfirmDialog(this, "Sei sicuro di voler eliminare questo ToDo?", "Conferma Eliminazione", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            controller.rimuoviToDo(selectedBoardTitle, toDoDaRimuovere);
        }
    }

    /**
     * Mostra un dialogo per condividere un ToDo con i contatti esistenti o con una nuova email.
     *
     * @param todo il ToDo da condividere.
     */
    private void condividiToDo(ToDo todo) {
        ListaUtenti contatti = controller.getContatti();
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Seleziona i contatti o inserisci una nuova email:"), BorderLayout.NORTH);

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

        JPanel emailInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        emailInputPanel.setBorder(BorderFactory.createTitledBorder("Condividi con una nuova email"));
        JTextField emailField = new JTextField(25);
        emailInputPanel.add(new JLabel("Email:"));
        emailInputPanel.add(emailField);

        panel.add(new JScrollPane(contattiPanel), BorderLayout.CENTER);
        panel.add(emailInputPanel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel, "Condividi ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            checkBoxes.stream().filter(JCheckBox::isSelected).forEach(cb -> controller.condividiToDo(todo, cb.getText()));
            String nuovaEmail = emailField.getText().trim();
            if (!nuovaEmail.isEmpty()) {
                controller.condividiToDo(todo, nuovaEmail);
            }
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante per modificare la descrizione della bacheca.
     *
     * @param unused l'evento di azione (non utilizzato).
     */
    private void modificaDescrizioneBachecaSelezionata(java.awt.event.ActionEvent unused) {
        if (selectedBoardTitle == null) return;
        Bacheca bachecaCorrente = controller.getBacheche().get(selectedBoardTitle);
        String descrizioneAttuale = bachecaCorrente.getDescrizione();

        Object input = JOptionPane.showInputDialog(this, "Modifica descrizione per la bacheca '" + selectedBoardTitle.name() + "':", MODIFY_BOARD_DESC_TITLE, JOptionPane.PLAIN_MESSAGE, null, null, descrizioneAttuale);
        if (input != null) {
            controller.modificaDescrizioneBacheca(selectedBoardTitle, input.toString());
        }
    }

    /**
     * Gestisce l'evento di logout, chiedendo conferma all'utente.
     *
     * @param unused l'evento di azione (non utilizzato).
     */
    private void performLogout(java.awt.event.ActionEvent unused) {
        int confirm = JOptionPane.showConfirmDialog(this, "Sei sicuro di voler effettuare il logout?", "Conferma Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.eseguiLogout();
        }
    }

    /**
     * Mostra una semplice finestra di dialogo con le informazioni del profilo dell'utente.
     */
    private void showProfileDialog() {
        String userEmail = controller.getUtenteCorrente() != null ? controller.getUtenteCorrente().getEmail() : "N/A";
        JOptionPane.showMessageDialog(this, "Email Utente: " + userEmail, "Profilo Personale", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Metodo chiamato dal controller per notificare che la lista contatti è stata aggiornata.
     * Attualmente logga solo un messaggio.
     */
    public void refreshContatti() {
        LOGGER.log(Level.INFO, "Contact list refreshed.");
    }

    /**
     * Mostra una finestra di dialogo per la gestione dei contatti (aggiunta/rimozione).
     */
    private void showContactsDialog() {
        JDialog contactsDialog = new JDialog(this, "Gestione Contatti", true);
        contactsDialog.setSize(400, 300);
        contactsDialog.setLocationRelativeTo(this);
        contactsDialog.setLayout(new BorderLayout());

        DefaultListModel<String> contactsListModel = new DefaultListModel<>();
        JList<String> contactsList = new JList<>(contactsListModel);
        populateContactsList(contactsListModel);

        contactsDialog.add(new JScrollPane(contactsList), BorderLayout.CENTER);

        JPanel buttonPanel = createContactsButtonPanel(contactsDialog, contactsList, contactsListModel);
        contactsDialog.add(buttonPanel, BorderLayout.SOUTH);
        contactsDialog.setVisible(true);
    }

    /**
     * Popola il modello della lista dei contatti con i dati attuali.
     *
     * @param model il {@link DefaultListModel} da popolare.
     */
    private void populateContactsList(DefaultListModel<String> model) {
        model.removeAllElements();
        ListaUtenti contatti = controller.getContatti();
        if (contatti != null) {
            for (String contact : contatti.getLista()) {
                model.addElement(contact);
            }
        }
    }

    /**
     * Crea il pannello dei pulsanti per la finestra di dialogo dei contatti.
     *
     * @param parent il dialogo genitore.
     * @param list la lista dei contatti.
     * @param model il modello della lista.
     * @return un {@link JPanel} con i pulsanti Aggiungi e Rimuovi.
     */
    private JPanel createContactsButtonPanel(JDialog parent, JList<String> list, DefaultListModel<String> model) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton addButton = new JButton("Aggiungi Contatto");
        addButton.addActionListener(_ -> addContactAction(parent, model));
        buttonPanel.add(addButton);

        JButton removeButton = new JButton("Rimuovi Contatto");
        removeButton.addActionListener(_ -> removeContactAction(parent, list, model));
        buttonPanel.add(removeButton);

        return buttonPanel;
    }

    /**
     * Gestisce l'azione di aggiunta di un nuovo contatto.
     *
     * @param parent il dialogo genitore.
     * @param model il modello della lista da aggiornare.
     */
    private void addContactAction(JDialog parent, DefaultListModel<String> model) {
        String email = JOptionPane.showInputDialog(parent, "Inserisci l'email del contatto:");
        if (email != null && !email.trim().isEmpty()) {
            controller.aggiungiContatto(email.trim());
            populateContactsList(model);
        }
    }

    /**
     * Gestisce l'azione di rimozione di un contatto selezionato.
     *
     * @param parent il dialogo genitore.
     * @param list la lista da cui ottenere la selezione.
     * @param model il modello da cui rimuovere l'elemento.
     */
    private void removeContactAction(JDialog parent, JList<String> list, DefaultListModel<String> model) {
        String selectedContact = list.getSelectedValue();
        if (selectedContact != null) {
            int confirm = JOptionPane.showConfirmDialog(parent, "Sei sicuro di voler rimuovere questo contatto?", "Conferma Rimozione", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.rimuoviContatto(selectedContact);
                model.removeElement(selectedContact);
            }
        } else {
            JOptionPane.showMessageDialog(parent, "Seleziona un contatto da rimuovere.", "Attenzione", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Classe interna che definisce come un singolo ToDo viene renderizzato all'interno di una JList.
     * Estende JCheckBox per mostrare lo stato di completamento in modo interattivo.
     * Gestisce la colorazione dello sfondo in base al colore del ToDo, allo stato di selezione
     * e allo stato di scadenza (evidenzia in rosso i ToDo scaduti e non completati).
     */
    public static class ToDoCellRenderer extends JCheckBox implements ListCellRenderer<ToDo> {
        private static final DateTimeFormatter cellDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        /**
         * Costruttore del renderer. Imposta il componente come opaco.
         */
        public ToDoCellRenderer() {
            setOpaque(true);
        }

        /**
         * Configura il componente per visualizzare un ToDo.
         *
         * @param list la JList in cui viene disegnato il componente.
         * @param todo l'oggetto ToDo da visualizzare.
         * @param index l'indice della cella.
         * @param isSelected true se la cella è selezionata.
         * @param cellHasFocus true se la cella ha il focus.
         * @return il Componente configurato per la visualizzazione.
         */
        @Override
        public Component getListCellRendererComponent(JList<? extends ToDo> list, ToDo todo, int index, boolean isSelected, boolean cellHasFocus) {
            if (todo != null) {
                setSelected(Boolean.TRUE.equals(todo.getStato()));
                String scadenzaStr = (todo.getScadenza() != null) ? todo.getScadenza().format(cellDateFormatter) : "N/D";
                setText(String.format("%s (Scad: %s) - %s", todo.getTitolo(), scadenzaStr, todo.getDescrizione()));

                Color bgColor;
                // Controlla se il ToDo è scaduto e non completato
                if (todo.getScadenza() != null && todo.getScadenza().isBefore(LocalDate.now()) && !Boolean.TRUE.equals(todo.getStato())) {
                    bgColor = new Color(255, 182, 193); // Rosso chiaro per evidenziare
                } else {
                    bgColor = todo.getColore() != null ? todo.getColore() : list.getBackground();
                }

                setBackground(isSelected ? list.getSelectionBackground() : bgColor);

                if (isSelected) {
                    setForeground(list.getSelectionForeground());
                } else {
                    setForeground(isColorDark(bgColor) ? Color.WHITE : Color.BLACK);
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

        /**
         * Determina se un colore è scuro calcolando la sua luminanza.
         * Questo aiuta a decidere se usare testo bianco o nero per la leggibilità.
         *
         * @param color il Colore da analizzare.
         * @return true se il colore è considerato scuro, false altrimenti.
         */
        public static boolean isColorDark(Color color) {
            if (color == null) return false;
            double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
            return luminance < 0.5;
        }
    }
}


/**
 * Classe record per associare un nome leggibile a un oggetto {@link Color}.
 * Viene utilizzata nella JComboBox per permettere all'utente di selezionare un colore per il ToDo.
 * L'override del metodo toString() è fondamentale per la visualizzazione nella JComboBox.
 *
 * @param name il nome del colore da visualizzare (es. "Rosso Chiaro").
 * @param color l'oggetto {@link Color} corrispondente.
 */
record NamedColor(String name, Color color) {
    /**
     * Restituisce il nome del colore. Questo metodo viene chiamato dalla JComboBox
     * per visualizzare ogni elemento nella lista a discesa.
     * @return il nome del colore.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Fornisce un array di colori predefiniti disponibili per la selezione da parte dell'utente.
     * @return un array di oggetti {@link NamedColor}.
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
     * Trova un oggetto NamedColor corrispondente a un dato oggetto Color.
     * Se il colore non viene trovato, restituisce un valore predefinito (Bianco).
     * @param colorToFind il Colore da cercare.
     * @return l'oggetto {@link NamedColor} corrispondente, o il primo della lista se non trovato.
     */
    public static NamedColor findNamedColor(Color colorToFind) {
        if (colorToFind == null) {
            return getPredefinedColors()[0];
        }
        for (NamedColor namedColor : getPredefinedColors()) {
            if (namedColor.color().equals(colorToFind)) {
                return namedColor;
            }
        }
        return getPredefinedColors()[0];
    }
}