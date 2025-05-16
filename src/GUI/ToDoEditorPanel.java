package gui;

import javax.swing.*;
import java.awt.*;
import Controller.Controller;

public class ToDoEditorPanel extends JDialog {

    private JTextField titoloField;
    private JTextArea descrizioneArea;
    private Controller controller;
    private BachecaPanel parent;

    public ToDoEditorPanel(Controller controller, BachecaPanel parent) {
        this.controller = controller;
        this.parent = parent;
        setTitle("Nuovo ToDo");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setModal(true);

        JPanel panel = new JPanel(new BorderLayout());

        titoloField = new JTextField();
        descrizioneArea = new JTextArea(5, 20);

        panel.add(new JLabel("Titolo:"), BorderLayout.NORTH);
        panel.add(titoloField, BorderLayout.CENTER);

        JPanel areaPanel = new JPanel(new BorderLayout());
        areaPanel.add(new JLabel("Descrizione:"), BorderLayout.NORTH);
        areaPanel.add(new JScrollPane(descrizioneArea), BorderLayout.CENTER);

        JButton saveButton = new JButton("Salva");
        saveButton.addActionListener(e -> {
            controller.aggiungiToDo(titoloField.getText(), descrizioneArea.getText());
            parent.refreshData();
            dispose();
        });

        JPanel southPanel = new JPanel();
        southPanel.add(saveButton);

        add(panel, BorderLayout.NORTH);
        add(areaPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }
}
