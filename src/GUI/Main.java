package gui;

import controller.Controller;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Classe principale che funge da punto di ingresso (entry point) per l'applicazione GUI.
 * Il suo compito è inizializzare e avviare l'interfaccia grafica in modo sicuro
 * sul thread di gestione degli eventi di Swing (Event Dispatch Thread).
 */
public class Main {
    /**
     * Il metodo principale dell'applicazione.
     * <p>
     * Vengono eseguite le seguenti operazioni:
     * <ol>
     * <li>Imposta il Look and Feel "Nimbus" per dare all'interfaccia un aspetto moderno.
     * In caso di fallimento, l'eccezione viene stampata sullo standard error.</li>
     * <li>Utilizza {@link SwingUtilities#invokeLater(Runnable)} per accodare l'inizializzazione
     * del {@link Controller} e della {@link View} sull'Event Dispatch Thread (EDT).
     * Questo è fondamentale per garantire la thread-safety delle componenti Swing.</li>
     * </ol>
     *
     * @param args gli argomenti passati dalla riga di comando (non utilizzati in questa applicazione).
     */
    public static void main(String[] args) {
        // Imposta il Look and Feel Nimbus per un aspetto più moderno
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Gestisce l'eccezione, ad esempio stampandola o mostrando un messaggio
            e.printStackTrace();
        }

        // Avvia l'applicazione sull'Event Dispatch Thread (EDT) di Swing
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Controller controller = new Controller();
                controller.init();
            }
        });
    }
}