package gui;

import controller.Controller;
import org.ToDo.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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


public class View extends JFrame {
    private final Controller controller;
    private JPanel mainBoardsPanel;
    private JPanel boardSelectionContainer;
    private JPanel todoListPanel;
    private JSplitPane mainSplitPane;
    private JCheckBox showCompletedCheckBox;
    private DefaultListModel<ToDo> todoListModel;
    private JList<ToDo> todoList;
    private JButton addToDoButton;
    private JLabel currentBoardDescriptionLabel;
    private JButton modifyBoardDescriptionButton;
    private Titolo selectedBoardTitle;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NamedColor[] predefinedColors = NamedColor.getPredefinedColors();
    private boolean isTileView = false;
    private JToggleButton viewToggleButton;
    private byte[] selectedImageBytes;
    private JDialog contattiDialog;
    private DefaultListModel<String> contattiListModel;
    private JList<String> contattiList;


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
        homeButton.setFocusPainted(false);
        homeButton.addActionListener(e -> showBoardSelectionView());
        buttonContainer.add(homeButton);

        JButton profileButton = new JButton("Profilo");
        profileButton.setFocusPainted(false);
        profileButton.addActionListener(e -> showProfileDialog());
        buttonContainer.add(profileButton);

        JButton allIncompleteToDosButton = new JButton("ToDo");
        allIncompleteToDosButton.setFocusPainted(false);
        allIncompleteToDosButton.addActionListener(e -> showAllIncompleteToDos());
        buttonContainer.add(allIncompleteToDosButton);

        viewToggleButton = new JToggleButton("Vista Riquadri");
        viewToggleButton.setFocusPainted(false);
        viewToggleButton.addActionListener(e -> {
            isTileView = viewToggleButton.isSelected();
            refreshToDoList();
        });
        buttonContainer.add(viewToggleButton);

        JButton logoutButtonTop = new JButton("Logout");
        logoutButtonTop.setForeground(Color.RED);
        logoutButtonTop.setFocusPainted(false);
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
        SwingUtilities.invokeLater(() -> mainSplitPane.setDividerLocation(1.0));
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

    private void showToDoListForBoard() {
        boardSelectionContainer.setLayout(new GridLayout(0, 1, 0, 20));
        boardSelectionContainer.revalidate();
        boardSelectionContainer.repaint();

        mainSplitPane.setDividerSize(8);
        mainSplitPane.setEnabled(true);
        mainSplitPane.setResizeWeight(0.2);
        mainSplitPane.setDividerLocation(0.2);

        todoListPanel.removeAll();
        todoListPanel.setLayout(new BorderLayout());

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
                    Rectangle cellBounds = list.getCellBounds(index, index);
                    if (cellBounds != null && e.getY() >= cellBounds.y && e.getY() < (cellBounds.y + cellBounds.height)) {
                        ToDo todo = list.getModel().getElementAt(index);
                        int checkBoxGraphicClickWidth = 25;
                        if (e.getX() - cellBounds.x >= 0 && e.getX() - cellBounds.x < checkBoxGraphicClickWidth) {
                            controller.toggleToDoStatus(selectedBoardTitle, todo, !todo.getStato());
                        } else {
                            showToDoDetailDialog(todo);
                        }
                    }
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addToDoButton = new JButton("<html><span style='font-size:1.2em;'>&#43;</span> Aggiungi ToDo</html>");
        addToDoButton.addActionListener(this::aggiungiToDo);
        buttonPanel.add(addToDoButton);
        todoListPanel.add(buttonPanel, BorderLayout.SOUTH);

        refreshToDoList();
        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    private void showAllIncompleteToDos() {
        selectedBoardTitle = null;
        boardSelectionContainer.setLayout(new GridLayout(0, 1, 0, 20));
        boardSelectionContainer.revalidate();
        boardSelectionContainer.repaint();

        mainSplitPane.setDividerSize(8);
        mainSplitPane.setEnabled(true);
        mainSplitPane.setResizeWeight(0.2);
        mainSplitPane.setDividerLocation(0.2);

        todoListPanel.removeAll();
        todoListPanel.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        currentBoardDescriptionLabel = new JLabel("Tutti i ToDo Incompiuti");
        modifyBoardDescriptionButton = new JButton("Modifica Descrizione");
        showCompletedCheckBox = new JCheckBox("Mostra ToDo Completati");

        JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        descriptionPanel.add(currentBoardDescriptionLabel);
        headerPanel.add(descriptionPanel, BorderLayout.NORTH);
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
                    Rectangle cellBounds = list.getCellBounds(index, index);
                    if (cellBounds != null && e.getY() >= cellBounds.y && e.getY() < (cellBounds.y + cellBounds.height)) {
                        ToDo todo = list.getModel().getElementAt(index);
                        int checkBoxGraphicClickWidth = 25;
                        if (e.getX() - cellBounds.x >= 0 && e.getX() - cellBounds.x < checkBoxGraphicClickWidth) {
                            controller.toggleToDoStatus(todo.getBachecaTitolo(), todo, !todo.getStato());
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

        refreshToDoList();
        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    private void aggiungiToDoGlobale() {
        Titolo[] bachecheDisponibili = Titolo.values();
        @SuppressWarnings("unchecked")
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

    public void refreshToDoList() {
        Component centerComponent = todoListPanel.getLayout() instanceof BorderLayout ? ((BorderLayout) todoListPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER) : null;
        if (centerComponent != null) {
            todoListPanel.remove(centerComponent);
        }

        if (selectedBoardTitle != null) {
            currentBoardDescriptionLabel.setText("Descrizione: " + controller.getBacheche().get(selectedBoardTitle).getDescrizione());
            modifyBoardDescriptionButton.setVisible(true);
            showCompletedCheckBox.setVisible(true);
        } else {
            currentBoardDescriptionLabel.setText("Tutti i ToDo Incompiuti");
            if (modifyBoardDescriptionButton != null) modifyBoardDescriptionButton.setVisible(false);
            if (showCompletedCheckBox != null) showCompletedCheckBox.setVisible(false);
        }

        if (isTileView) {
            JPanel cardsContainer = new JPanel(new GridLayout(0, 2, 10, 10));
            cardsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            if (selectedBoardTitle == null) {
                for (Bacheca bacheca : controller.getBacheche().values()) {
                    for (ToDo t : bacheca.getToDos()) {
                        if (!t.getStato()) {
                            cardsContainer.add(createToDoCard(t));
                        }
                    }
                }
            } else {
                Bacheca bacheca = controller.getBacheche().get(selectedBoardTitle);
                if (bacheca != null) {
                    for (ToDo t : bacheca.getToDos()) {
                        if (showCompletedCheckBox.isSelected() || !t.getStato()) {
                            cardsContainer.add(createToDoCard(t));
                        }
                    }
                }
            }
            todoListPanel.add(new JScrollPane(cardsContainer), BorderLayout.CENTER);
        } else {
            todoListModel.clear();
            if (selectedBoardTitle == null) {
                for (Bacheca bacheca : controller.getBacheche().values()) {
                    for (ToDo t : bacheca.getToDos()) {
                        if (!t.getStato()) {
                            todoListModel.addElement(t);
                        }
                    }
                }
            } else {
                Bacheca bacheca = controller.getBacheche().get(selectedBoardTitle);
                if (bacheca != null) {
                    for (ToDo t : bacheca.getToDos()) {
                        if (showCompletedCheckBox.isSelected() || !t.getStato()) {
                            todoListModel.addElement(t);
                        }
                    }
                }
            }
            todoListPanel.add(new JScrollPane(todoList), BorderLayout.CENTER);
        }
        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    private JPanel createToDoCard(ToDo todo) {
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        cardPanel.setPreferredSize(new Dimension(180, 120));
        cardPanel.setBackground(todo.getColore() != null ? todo.getColore() : Color.WHITE);

        if (todo.getImmagine() != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(todo.getImmagine());
                BufferedImage bImage = ImageIO.read(bis);
                ImageIcon icon = new ImageIcon(bImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                JLabel imageLabel = new JLabel(icon);
                imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                cardPanel.add(imageLabel);
                cardPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JLabel titleLabel = new JLabel("<html><b>" + todo.getTitolo() + "</b></html>");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel descLabel = new JLabel("<html>" + todo.getDescrizione() + "</html>");
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        Color bgColor = todo.getColore();
        if (bgColor != null && ToDoCellRenderer.isColorDark(bgColor)) {
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
            public void mouseClicked(MouseEvent e) {
                showToDoDetailDialog(todo);
            }
        });
        return cardPanel;
    }

    private void showToDoDetailDialog(ToDo todo) {
        JDialog detailDialog = new JDialog(this, "Dettagli ToDo: " + todo.getTitolo(), true);
        detailDialog.setSize(500, 550);
        detailDialog.setLocationRelativeTo(this);
        detailDialog.setLayout(new BorderLayout(10, 10));
        detailDialog.setResizable(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        infoPanel.add(new JLabel("<html><b>Titolo:</b> " + todo.getTitolo() + "</html>"));
        infoPanel.add(new JLabel("<html><b>Descrizione:</b> " + todo.getDescrizione() + "</html>"));
        infoPanel.add(new JLabel("<html><b>Autore:</b> " + todo.getListaUtenti().getAutore() + "</html>"));
        infoPanel.add(new JLabel("<html><b>Condiviso con:</b> " + (todo.getListaUtenti().getLista().isEmpty() ? "Nessuno" : String.join(", ", todo.getListaUtenti().getLista())) + "</html>"));
        infoPanel.add(new JLabel("<html><b>Stato:</b> " + (todo.getStato() ? "Completato" : "Incompleto") + "</html>"));
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
                        }
                    } catch (URISyntaxException | IOException ex) {
                        JOptionPane.showMessageDialog(detailDialog, "Errore nell'apertura dell'URL.", "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        }
        infoPanel.add(urlLabel);
        infoPanel.add(new JLabel("<html><b>Posizione:</b> " + (todo.getPosizione() != null && !todo.getPosizione().isEmpty() ? todo.getPosizione().trim() : "N/D") + "</html>"));
        String hexColor = String.format("#%06x", todo.getColore().getRGB() & 0xFFFFFF);
        infoPanel.add(new JLabel("<html><b>Colore:</b> <font color='" + hexColor + "'>&#9632;</font> " + NamedColor.findNamedColor(todo.getColore()).getName() + "</html>"));

        if (todo.getImmagine() != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(todo.getImmagine());
                BufferedImage bImage = ImageIO.read(bis);
                ImageIcon icon = new ImageIcon(bImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH));
                infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                infoPanel.add(new JLabel(icon));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        detailDialog.add(new JScrollPane(infoPanel), BorderLayout.CENTER);

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
            controller.toggleToDoStatus(todo.getBachecaTitolo(), todo, !todo.getStato());
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
        @SuppressWarnings("unchecked")
        JComboBox<NamedColor> colorSelector = new JComboBox<>(predefinedColors);
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

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
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
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        colorPanel.add(new JLabel("Colore:"));
        colorPanel.add(colorSelector);
        colorPanel.add(colorPreviewPanel);
        panel.add(colorPanel);

        JButton allegaImmagineButton = new JButton("Allega Immagine");
        JLabel immagineSelezionataLabel = new JLabel("Nessuna immagine selezionata");
        selectedImageBytes = null; // Resetta l'immagine
        allegaImmagineButton.addActionListener(ev -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Immagini", "jpg", "png", "gif", "bmp"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    BufferedImage bImage = ImageIO.read(selectedFile);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ImageIO.write(bImage, "png", bos);
                    selectedImageBytes = bos.toByteArray();
                    immagineSelezionataLabel.setText(selectedFile.getName());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Errore nel caricamento dell'immagine.", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        imagePanel.add(allegaImmagineButton);
        imagePanel.add(immagineSelezionataLabel);
        panel.add(imagePanel);

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
                        selectedImageBytes,
                        selectedBoardTitle,
                        controller.getUtenteCorrente().getEmail()
                );
                controller.aggiungiToDo(selectedBoardTitle, nuovo);
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
        @SuppressWarnings("unchecked")
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

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Titolo:")); panel.add(titoloField);
        panel.add(new JLabel("Descrizione:")); panel.add(descrizioneField);
        panel.add(new JLabel("Data Scadenza (gg/mm/aaaa):")); panel.add(dataScadenzaField);
        panel.add(new JLabel("URL (opzionale):")); panel.add(urlField);
        panel.add(new JLabel("Posizione (opzionale):")); panel.add(posizioneField);
        panel.add(completatoCheckBoxDialog);
        JPanel colorPanelModify = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        colorPanelModify.add(new JLabel("Colore:"));
        colorPanelModify.add(colorSelectorModify);
        colorPanelModify.add(colorPreviewPanelModify);
        panel.add(colorPanelModify);

        JButton allegaImmagineButton = new JButton("Modifica Immagine");
        JLabel immagineSelezionataLabel = new JLabel(toDoDaModificare.getImmagine() != null ? "Immagine presente" : "Nessuna immagine");
        selectedImageBytes = toDoDaModificare.getImmagine();
        allegaImmagineButton.addActionListener(ev -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Immagini", "jpg", "png", "gif", "bmp"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    BufferedImage bImage = ImageIO.read(selectedFile);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ImageIO.write(bImage, "png", bos);
                    selectedImageBytes = bos.toByteArray();
                    immagineSelezionataLabel.setText("Nuova immagine: " + selectedFile.getName());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        imagePanel.add(allegaImmagineButton);
        imagePanel.add(immagineSelezionataLabel);
        panel.add(imagePanel);

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
                    JOptionPane.showMessageDialog(this, "Formato data non valido. La data di scadenza non sarà modificata.", "Errore Data", JOptionPane.ERROR_MESSAGE);
                }
            } else if (toDoDaModificare.getScadenza() != null && dataInput.isEmpty()) {
                nuovaScadenza = null;
                JOptionPane.showMessageDialog(this, "Il campo data è stato lasciato vuoto. La data di scadenza è stata rimossa.", "Info Data", JOptionPane.INFORMATION_MESSAGE);
            }
            controller.modificaToDo(toDoDaModificare.getBachecaTitolo(), toDoDaModificare, nuovoTitolo, nuovaDescrizione, nuovaScadenza, nuovoStato, nuovoColore, nuovoUrl, nuovaPosizione, selectedImageBytes);
        }
    }

    private void spostaToDo(ToDo toDoDaSpostare) {
        if (toDoDaSpostare == null) return;
        Titolo bachecaOrigine = toDoDaSpostare.getBachecaTitolo();
        ArrayList<Titolo> opzioniDestinazioneList = new ArrayList<>(Arrays.asList(Titolo.values()));
        opzioniDestinazioneList.remove(bachecaOrigine);
        if (opzioniDestinazioneList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Non ci sono altre bacheche disponibili.", "Informazione", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Titolo bachecaDestinazione = (Titolo) JOptionPane.showInputDialog(this, "Sposta in bacheca:", "Sposta ToDo", JOptionPane.PLAIN_MESSAGE, null, opzioniDestinazioneList.toArray(), opzioniDestinazioneList.get(0));
        if (bachecaDestinazione != null) {
            controller.spostaToDoGUI(bachecaOrigine, toDoDaSpostare, bachecaDestinazione);
        }
    }

    private void rimuoviToDo(ToDo toDoDaRimuovere) {
        if (toDoDaRimuovere == null) return;
        int choice = JOptionPane.showConfirmDialog(this, "Sei sicuro di voler eliminare questo ToDo?", "Conferma Eliminazione", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            controller.rimuoviToDo(toDoDaRimuovere.getBachecaTitolo(), toDoDaRimuovere);
        }
    }

    private void condividiToDo(ToDo todo) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Condividi con:"));

        JComboBox<String> contattiDropdown = new JComboBox<>();
        contattiDropdown.addItem("Digita un'email...");
        controller.getContatti().getLista().forEach(contattiDropdown::addItem);

        JTextField emailField = new JTextField();

        contattiDropdown.addActionListener(e -> {
            if (contattiDropdown.getSelectedIndex() > 0) {
                emailField.setText((String) contattiDropdown.getSelectedItem());
                emailField.setEnabled(false);
            } else {
                emailField.setText("");
                emailField.setEnabled(true);
            }
        });

        panel.add(new JLabel("Scegli dalla lista:"));
        panel.add(contattiDropdown);
        panel.add(new JLabel("Oppure inserisci una nuova email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Condividi ToDo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String userEmail = emailField.getText().trim();
            if (userEmail != null && !userEmail.isEmpty()) {
                controller.condividiToDo(todo, userEmail);
            } else {
                JOptionPane.showMessageDialog(this, "Nessuna email inserita.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void modificaDescrizioneBachecaSelezionata(ActionEvent e) {
        if (selectedBoardTitle == null) return;
        Bacheca bachecaCorrente = controller.getBacheche().get(selectedBoardTitle);
        if (bachecaCorrente == null) return;
        String descrizioneAttuale = bachecaCorrente.getDescrizione();
        Object input = JOptionPane.showInputDialog(this, "Modifica descrizione per la bacheca '" + selectedBoardTitle.name() + "':", "Modifica Descrizione Bacheca", JOptionPane.PLAIN_MESSAGE, null, null, descrizioneAttuale);
        if (input != null) {
            controller.modificaDescrizioneBacheca(selectedBoardTitle, input.toString());
        }
    }

    private void performLogout(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(this, "Sei sicuro di voler effettuare il logout?", "Conferma Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            controller.eseguiLogout();
        }
    }

    private void showProfileDialog() {
        JDialog profileDialog = new JDialog(this, "Profilo Personale", true);
        profileDialog.setSize(300, 200);
        profileDialog.setLocationRelativeTo(this);
        profileDialog.setLayout(new BorderLayout(10, 10));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        String userEmail = controller.getUtenteCorrente() != null ? controller.getUtenteCorrente().getEmail() : "N/A";
        infoPanel.add(new JLabel("Email Utente: " + userEmail));

        profileDialog.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton contattiButton = new JButton("Gestisci Contatti");
        contattiButton.addActionListener(e -> {
            profileDialog.dispose();
            showGestioneContattiDialog();
        });
        buttonPanel.add(contattiButton);

        profileDialog.add(buttonPanel, BorderLayout.SOUTH);
        profileDialog.setVisible(true);
    }

    private void showGestioneContattiDialog() {
        contattiDialog = new JDialog(this, "Gestione Contatti", true);
        contattiDialog.setSize(400, 300);
        contattiDialog.setLocationRelativeTo(this);
        contattiDialog.setLayout(new BorderLayout(10, 10));

        contattiListModel = new DefaultListModel<>();
        controller.getContatti().getLista().forEach(contattiListModel::addElement);
        contattiList = new JList<>(contattiListModel);

        JScrollPane scrollPane = new JScrollPane(contattiList);
        contattiDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton aggiungiButton = new JButton("Aggiungi");
        aggiungiButton.addActionListener(e -> {
            String email = JOptionPane.showInputDialog(contattiDialog, "Inserisci l'email del contatto da aggiungere:");
            if (email != null && !email.trim().isEmpty()) {
                controller.aggiungiContatto(email.trim());
            }
        });

        JButton rimuoviButton = new JButton("Rimuovi");
        rimuoviButton.addActionListener(e -> {
            String selected = contattiList.getSelectedValue();
            if (selected != null) {
                int choice = JOptionPane.showConfirmDialog(contattiDialog, "Sei sicuro di voler rimuovere questo contatto?", "Conferma Rimozione", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    controller.rimuoviContatto(selected);
                }
            } else {
                JOptionPane.showMessageDialog(contattiDialog, "Seleziona un contatto da rimuovere.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(aggiungiButton);
        buttonPanel.add(rimuoviButton);
        contattiDialog.add(buttonPanel, BorderLayout.SOUTH);

        contattiDialog.setVisible(true);
    }

    public void refreshContatti() {
        if (contattiListModel != null) {
            contattiListModel.clear();
            controller.getContatti().getLista().forEach(contattiListModel::addElement);
        }
    }


    public static class ToDoCellRenderer extends JCheckBox implements ListCellRenderer<ToDo> {
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        public ToDoCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ToDo> list, ToDo todo, int index, boolean isSelected, boolean cellHasFocus) {
            if (todo != null) {
                setSelected(todo.getStato());
                String scadenzaStr = (todo.getScadenza() != null) ? todo.getScadenza().format(dateFormatter) : "N/D";
                setText(String.format("%s (Scad: %s) - %s", todo.getTitolo(), scadenzaStr, todo.getDescrizione()));
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    Color bgColor = todo.getColore() != null ? todo.getColore() : list.getBackground();
                    setBackground(bgColor);
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

        public static boolean isColorDark(Color color) {
            if (color == null) return false;
            return (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255 < 0.5;
        }
    }
}