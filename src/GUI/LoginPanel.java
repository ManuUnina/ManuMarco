package gui;

import javax.swing.*;
import java.awt.*;
import controller.Controller;

public class LoginPanel extends JPanel {

    private Controller controller;
    private MainFrame mainFrame;
    private JTextField usernameField;

    public LoginPanel(Controller controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel label = new JLabel("Inserisci username:");
        usernameField = new JTextField(15);
        JButton loginButton = new JButton("Entra");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                controller.login(username);
                mainFrame.showBacheca();
            }
        });

        gbc.gridx = 0; gbc.gridy = 0;
        add(label, gbc);
        gbc.gridy = 1;
        add(usernameField, gbc);
        gbc.gridy = 2;
        add(loginButton, gbc);
    }
}
