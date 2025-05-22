package controller;

import org.ToDo.Bacheca;
import org.ToDo.Titolo;
import org.ToDo.ToDo;
import org.ToDo.Utente;
import gui.View;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.awt.Color; // Importa la classe Color
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;


public class Controller {
    private View view;
    private final Map<Titolo, Bacheca> bacheche = new EnumMap<>(Titolo.class);

    public Controller() {
        for (Titolo t : Titolo.values()) {
            bacheche.put(t, new Bacheca(t, " " + t.name()));
        }
        Utente.popolaUtentiIniziali();
    }

    public void init() {
        // ... (codice del costruttore e di init invariato) ...
        while (true) {
            boolean autenticazioneRiuscita = gestisciAutenticazione();

            if (autenticazioneRiuscita) {
                view = new View(this);
                view.setVisible(true);
                return;
            } else {
                int sceltaUscita = JOptionPane.showConfirmDialog(
                        null,
                        "Accesso non completato. Desideri uscire dall'applicazione?",
                        "Esci o Riprova?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (sceltaUscita == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(null, "L'applicazione si chiuderà.",
                            "Chiusura Applicazione", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                    return;
                }
            }
        }
    }

    private boolean gestisciAutenticazione() {
        // ... (codice invariato) ...
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
        // ... (codice invariato) ...
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

            if (Utente.autenticaUtente(email, password)) {
                JOptionPane.showMessageDialog(null, "Login effettuato con successo!", "Accesso Confermato", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Credenziali non valide. Riprova.", "Errore di Login", JOptionPane.ERROR_MESSAGE);
                return eseguiLoginDialog(); // Riprova login
            }
        }
        return false;
    }

    private boolean eseguiRegistrazioneDialog() {
        // ... (codice invariato) ...
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

            if (Utente.registraNuovoUtente(email, password)) {
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "L'email '" + email + "' è già registrata. Scegli un'altra email o effettua il login.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE);
                return eseguiRegistrazioneDialog(); // Riprova registrazione
            }
        }
        return false;
    }

    public void eseguiLogout() {
        // ... (codice invariato) ...
        if (view != null) {
            view.dispose();
        }
        Utente.logout();
        JOptionPane.showMessageDialog(null, "Logout effettuato con successo.", "Logout", JOptionPane.INFORMATION_MESSAGE);
        // Riavvia il ciclo di autenticazione creando un nuovo controller e chiamando init.
        // Questo potrebbe non essere l'approccio ideale per applicazioni complesse (meglio un gestore di stato/viste).
        new Controller().init();
    }

    public Utente getUtenteCorrente() {
        return Utente.getUtenteCorrenteAutenticato();
    }

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
        // ... (codice invariato) ...
        Bacheca bacheca = bacheche.get(titolo);
        if (bacheca != null && index >= 0 && index < bacheca.getToDos().size()) {
            bacheca.rimuoviToDo(index);
        }
    }

    public ToDo getToDoFromBacheca(Titolo titoloBacheca, int todoIndex) {
        // ... (codice invariato) ...
        Bacheca bacheca = bacheche.get(titoloBacheca);
        if (bacheca != null && todoIndex >= 0 && todoIndex < bacheca.getToDos().size()) {
            return bacheca.getToDos().get(todoIndex);
        }
        return null;
    }

    // Firma modificata per includere scadenza, stato e COLORE
    public void modificaToDo(Titolo titoloBacheca, int todoIndex, String nuovoTitolo, String nuovaDescrizione, LocalDate nuovaScadenza, boolean nuovoStato, Color nuovoColore) { // Aggiunto Color nuovoColore
        Bacheca bacheca = bacheche.get(titoloBacheca);
        if (bacheca != null) {
            // Assicurati che l'indice sia valido
            if (todoIndex >= 0 && todoIndex < bacheca.getToDos().size()) {
                ToDo toDoDaModificare = bacheca.getToDos().get(todoIndex);
                if (toDoDaModificare != null) {
                    toDoDaModificare.setTitolo(nuovoTitolo);
                    toDoDaModificare.setDescrizione(nuovaDescrizione);
                    toDoDaModificare.setScadenza(nuovaScadenza);
                    toDoDaModificare.setStato(nuovoStato);
                    toDoDaModificare.setColore(nuovoColore); // Imposta nuovo colore
                }
            } else {
                System.err.println("Indice ToDo non valido per la modifica: " + todoIndex);
            }
        }
    }

    public void spostaToDoGUI(Titolo bachecaOrigineTitolo, int todoIndex, Titolo bachecaDestinazioneTitolo) {
        // ... (codice invariato) ...
        Bacheca bachecaOrigine = bacheche.get(bachecaOrigineTitolo);
        Bacheca bachecaDestinazione = bacheche.get(bachecaDestinazioneTitolo);

        if (bachecaOrigine != null && bachecaDestinazione != null) {
            if (todoIndex >= 0 && todoIndex < bachecaOrigine.getToDos().size()) {
                ToDo toDoDaSpostare = bachecaOrigine.getToDos().remove(todoIndex);
                bachecaDestinazione.aggiungiToDo(toDoDaSpostare);
            } else {
                System.err.println("Indice ToDo non valido per lo spostamento.");
            }
        } else {
            System.err.println("Bacheca di origine o destinazione non valida per lo spostamento.");
        }
    }

    public void modificaDescrizioneBacheca(Titolo titoloBacheca, String nuovaDescrizione) {
        // ... (codice invariato) ...
        Bacheca bacheca = bacheche.get(titoloBacheca);
        if (bacheca != null) {
            bacheca.setDescrizione(nuovaDescrizione);
        } else {
            System.err.println("Tentativo di modificare la descrizione di una bacheca non esistente: " + titoloBacheca);
        }
    }
}