// Main.java
package gui;

import controller.Controller;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Controller controller = new Controller();
                controller.init(); // <--- USA QUESTO!
            }
        });
    }
}