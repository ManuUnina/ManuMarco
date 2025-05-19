// Main.java
package gui;

import controller.Controller;

import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Controller controller = new Controller();
            View view = new View(controller);
            view.setVisible(true);
        });
    }
}
