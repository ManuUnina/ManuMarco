package controller;

import org.ToDo.Bacheca;
import org.ToDo.Titolo;
import org.ToDo.ToDo;
import org.ToDo.Utente;
import gui.View;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.util.EnumMap;
import java.util.Map;

public class Controller {
    private View view;
    private final Map<Titolo, Bacheca> bacheche = new EnumMap<>(Titolo.class);
    // Rimosso: private List<Utente> utentiRegistrati = new ArrayList<>();
    // Rimosso: private Utente utenteCorrente; // Ora gestito staticamente in Utente.java

    public Controller() {
        // Inizializza le bacheche
        for (Titolo t : Titolo.values()) {
            bacheche.put(t, new Bacheca(t, "Descrizione bacheca: " + t.name()));
        }
        // Popola gli utenti di default tramite il metodo statico di Utente
        Utente.popolaUtentiIniziali();
    }

    public void init() {
        while (true) { // Ciclo principale per l'autenticazione
            boolean autenticazioneRiuscita = gestisciAutenticazione();

            if (autenticazioneRiuscita) {
                // Se l'autenticazione ha successo, crea e mostra la view principale
                // e esci dal ciclo di autenticazione (e dal metodo init)
                view = new View(this); // Assumendo che 'View' sia la tua classe GUI.View
                view.setVisible(true);
                return; // Esce dal metodo init, l'applicazione principale è avviata
            } else {
                // L'autenticazione non è riuscita (login fallito e annullato, registrazione annullata,
                // o l'utente ha chiuso il dialogo iniziale "Login/Registrati").
                // Chiediamo all'utente se vuole uscire dall'applicazione o riprovare.
                int sceltaUscita = JOptionPane.showConfirmDialog(
                        null, // Finestra genitore (null per centrarlo)
                        "Accesso non completata. Desideri uscire dall'applicazione?",
                        "Esci o Riprova?", // Titolo del dialogo
                        JOptionPane.YES_NO_OPTION, // Bottoni Sì/No
                        JOptionPane.QUESTION_MESSAGE); // Icona di domanda

                if (sceltaUscita == JOptionPane.YES_OPTION) {
                    // L'utente ha scelto di uscire
                    JOptionPane.showMessageDialog(null, "L'applicazione si chiuderà.",
                            "Chiusura Applicazione", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0); // Termina l'applicazione
                    return; // In realtà non necessario dopo System.exit(0), ma per coerenza logica
                }
                // Se l'utente sceglie NO (o chiude il dialogo di conferma uscita),
                // il ciclo 'while(true)' continuerà, e il metodo gestisciAutenticazione()
                // verrà chiamato di nuovo, ripresentando la schermata "Login/Registrati".
            }
        }
    }

    private boolean gestisciAutenticazione() {
        String[] opzioni = {"Login", "Registrati"};
        int scelta = JOptionPane.showOptionDialog(null, "Benvenuto! Cosa desideri fare?",
                "ToDo App",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, opzioni, opzioni[0]);

        if (scelta == 0) { // Login
            return eseguiLoginDialog();
        } else if (scelta == 1) { // Registrati
            if (eseguiRegistrazioneDialog()) {
                JOptionPane.showMessageDialog(null, "Registrazione completata! Ora effettua il login.",
                        "Registrazione Riuscita", JOptionPane.INFORMATION_MESSAGE);
                return eseguiLoginDialog();
            }
            return false;
        }
        return false;
    }

    private boolean eseguiLoginDialog() {
        JTextField emailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        Object[] messaggio = {
                "Email (Nome Utente):", emailField,
                "Password:", passwordField
        };

        int opzione = JOptionPane.showConfirmDialog(null, messaggio, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opzione == JOptionPane.OK_OPTION) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            // Usa il metodo statico di Utente per l'autenticazione
            if (Utente.autenticaUtente(email, password)) {
                JOptionPane.showMessageDialog(null, "Login effettuato con successo!", "Accesso Confermato", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Credenziali non valide. Riprova.", "Errore di Login", JOptionPane.ERROR_MESSAGE);
                return eseguiLoginDialog();
            }
        }
        return false;
    }

    private boolean eseguiRegistrazioneDialog() {
        JTextField emailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confermaPasswordField = new JPasswordField(20);
        Object[] messaggio = {
                "Email:", emailField,
                "Password (min. 3 caratteri):", passwordField,
                "Conferma Password:", confermaPasswordField
        };

        int opzione = JOptionPane.showConfirmDialog(null, messaggio, "Registrazione Nuovo Utente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opzione == JOptionPane.OK_OPTION) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confermaPassword = new String(confermaPasswordField.getPassword());

            if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(null, "Formato email non valido.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE);
                return eseguiRegistrazioneDialog();
            }
            if (password.isEmpty() || password.length() < 3) {
                JOptionPane.showMessageDialog(null, "La password deve contenere almeno 3 caratteri.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE);
                return eseguiRegistrazioneDialog();
            }
            if (!password.equals(confermaPassword)) {
                JOptionPane.showMessageDialog(null, "Le password non coincidono.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE);
                return eseguiRegistrazioneDialog();
            }

            // Usa il metodo statico di Utente per la registrazione
            if (Utente.registraNuovoUtente(email, password)) {
                // Il messaggio di successo è già gestito in gestisciAutenticazione
                return true;
            } else {
                // Se l'email è già registrata, mostra un messaggio e riprova
                JOptionPane.showMessageDialog(null, "L'email '" + email + "' è già registrata. Scegli un'altra email o effettua il login.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE);
                return eseguiRegistrazioneDialog();
            }
        }
        return false;
    }

    public Utente getUtenteCorrente() {
        return Utente.getUtenteCorrenteAutenticato(); // Prende l'utente corrente da Utente.java
    }

    // Metodi per la gestione delle bacheche e dei ToDo (rimangono nel controller)
    public Map<Titolo, Bacheca> getBacheche() {
        return bacheche;
    }

    public void aggiungiToDo(Titolo titolo, ToDo todo) {
        Bacheca bacheca = bacheche.get(titolo);
        if (bacheca != null) {
            bacheca.aggiungiToDo(todo);
        }
    }

    public void rimuoviToDo(Titolo titolo, int index) {
        Bacheca bacheca = bacheche.get(titolo);
        if (bacheca != null && index >= 0 && index < bacheca.getToDos().size()) {
            bacheca.rimuoviToDo(index);
        }
    }
}
