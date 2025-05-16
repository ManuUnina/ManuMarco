package gui;

import javax.swing.*;
import java.awt.*;
import Controller.Controller;

public class MainFrame extends JFrame {

    private Controller controller;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginPanel loginPanel;
    private BachecaPanel bachecaPanel;

    public MainFrame(Controller controller) {
        this.controller = controller;
        setTitle("ToDo App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(controller, this);
        bachecaPanel = new BachecaPanel(controller, this);

        mainPanel.add(loginPanel, "login");
        mainPanel.add(bachecaPanel, "bacheca");

        add(mainPanel);
        showLogin();
    }

    public void showLogin() {
        cardLayout.show(mainPanel, "login");
    }

    public void showBacheca() {
        bachecaPanel.refreshData();
        cardLayout.show(mainPanel, "bacheca");
    }
}