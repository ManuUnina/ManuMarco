package gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import org.ToDo.ToDo;
import Controller.Controller;


public class BachecaPanel extends JPanel {

    private Controller controller;
    private MainFrame mainFrame;
    private DefaultListModel<String> listModel;
    private JList<String> todoList;

    public BachecaPanel(Controller controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        todoList = new JList<>(listModel);
        add(new JScrollPane(todoList), BorderLayout.CENTER);

        JButton addButton = new JButton("Nuovo ToDo");
        addButton.addActionListener(e -> {
            new ToDoEditorPanel(controller, this).setVisible(true);
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> mainFrame.showLogin());

        JPanel buttons = new JPanel();
        buttons.add(addButton);
        buttons.add(logoutButton);
        add(buttons, BorderLayout.SOUTH);
    }

    public void refreshData() {
        listModel.clear();
        ArrayList<ToDo> todos = controller.getElencoToDoCorrente();
        for (ToDo t : todos) {
            listModel.addElement(t.getTitolo().getTitolo() + ": " + t.getDescrizione());
        }
    }
}