package controller;

import org.ToDo.Bacheca; //
import org.ToDo.Titolo; //
import org.ToDo.ToDo; //
import org.ToDo.Utente; //
import gui.View;

import javax.swing.JOptionPane; //
import javax.swing.JTextField; //
import javax.swing.JPasswordField; //
import java.util.EnumMap; //
import java.util.Map; //


public class Controller {
    private View view;
    private final Map<Titolo, Bacheca> bacheche = new EnumMap<>(Titolo.class); //

    public Controller() {
        for (Titolo t : Titolo.values()) { //
            bacheche.put(t, new Bacheca(t, "Descrizione bacheca: " + t.name())); //
        }
        Utente.popolaUtentiIniziali(); //
    }

    public void init() {
        while (true) { //
            boolean autenticazioneRiuscita = gestisciAutenticazione(); //

            if (autenticazioneRiuscita) { //
                view = new View(this); //
                view.setVisible(true); //
                return;
            } else { //
                int sceltaUscita = JOptionPane.showConfirmDialog( //
                        null,
                        "Accesso non completato. Desideri uscire dall'applicazione?",
                        "Esci o Riprova?",
                        JOptionPane.YES_NO_OPTION, //
                        JOptionPane.QUESTION_MESSAGE); //

                if (sceltaUscita == JOptionPane.YES_OPTION) { //
                    JOptionPane.showMessageDialog(null, "L'applicazione si chiuderà.", //
                            "Chiusura Applicazione", JOptionPane.INFORMATION_MESSAGE); //
                    System.exit(0); //
                    return;
                }
                // Se l'utente sceglie NO, il ciclo while(true) continua
            }
        }
    }


    private boolean gestisciAutenticazione() {
        String[] opzioni = {"Login", "Registrati"}; //
        int scelta = JOptionPane.showOptionDialog(null, "Benvenuto! Cosa desideri fare?", //
                "ToDo App", //
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, //
                null, opzioni, opzioni[0]); //

        if (scelta == 0) { // Login
            return eseguiLoginDialog(); //
        } else if (scelta == 1) { // Registrati
            if (eseguiRegistrazioneDialog()) { //
                JOptionPane.showMessageDialog(null, "Registrazione completata! Ora effettua il login.", //
                        "Registrazione Riuscita", JOptionPane.INFORMATION_MESSAGE); //
                return eseguiLoginDialog(); //
            }
            return false;
        }
        return false;
    }

    private boolean eseguiLoginDialog() {
        JTextField emailField = new JTextField(20); //
        JPasswordField passwordField = new JPasswordField(20); //
        Object[] messaggio = { //
                "Email (Nome Utente):", emailField, //
                "Password:", passwordField //
        };

        int opzione = JOptionPane.showConfirmDialog(null, messaggio, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); //
        if (opzione == JOptionPane.OK_OPTION) { //
            String email = emailField.getText().trim(); //
            String password = new String(passwordField.getPassword()); //

            if (Utente.autenticaUtente(email, password)) { //
                JOptionPane.showMessageDialog(null, "Login effettuato con successo!", "Accesso Confermato", JOptionPane.INFORMATION_MESSAGE); //
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Credenziali non valide. Riprova.", "Errore di Login", JOptionPane.ERROR_MESSAGE); //
                return eseguiLoginDialog(); //
            }
        }
        return false;
    }

    private boolean eseguiRegistrazioneDialog() {
        JTextField emailField = new JTextField(20); //
        JPasswordField passwordField = new JPasswordField(20); //
        JPasswordField confermaPasswordField = new JPasswordField(20); //
        Object[] messaggio = { //
                "Email:", emailField, //
                "Password (min. 3 caratteri):", passwordField, //
                "Conferma Password:", confermaPasswordField //
        };

        int opzione = JOptionPane.showConfirmDialog(null, messaggio, "Registrazione Nuovo Utente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); //
        if (opzione == JOptionPane.OK_OPTION) { //
            String email = emailField.getText().trim(); //
            String password = new String(passwordField.getPassword()); //
            String confermaPassword = new String(confermaPasswordField.getPassword()); //

            if (email.isEmpty() || !email.contains("@") || !email.contains(".")) { //
                JOptionPane.showMessageDialog(null, "Formato email non valido.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE); //
                return eseguiRegistrazioneDialog(); //
            }
            if (password.isEmpty() || password.length() < 3) { //
                JOptionPane.showMessageDialog(null, "La password deve contenere almeno 3 caratteri.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE); //
                return eseguiRegistrazioneDialog(); //
            }
            if (!password.equals(confermaPassword)) { //
                JOptionPane.showMessageDialog(null, "Le password non coincidono.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE); //
                return eseguiRegistrazioneDialog(); //
            }

            if (Utente.registraNuovoUtente(email, password)) { //
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "L'email '" + email + "' è già registrata. Scegli un'altra email o effettua il login.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE); //
                return eseguiRegistrazioneDialog();
            }
        }
        return false;
    }

    public void eseguiLogout() {
        if (view != null) { //
            view.dispose();
        }
        Utente.logout(); //
        JOptionPane.showMessageDialog(null, "Logout effettuato con successo.", "Logout", JOptionPane.INFORMATION_MESSAGE); //
        new Controller().init();
    }


    public Utente getUtenteCorrente() {
        return Utente.getUtenteCorrenteAutenticato(); //
    }

    public Map<Titolo, Bacheca> getBacheche() {
        return bacheche; //
    }

    public void aggiungiToDo(Titolo titolo, ToDo todo) {
        Bacheca bacheca = bacheche.get(titolo); //
        if (bacheca != null) { //
            bacheca.aggiungiToDo(todo); //
        }
    }

    public void rimuoviToDo(Titolo titolo, int index) {
        Bacheca bacheca = bacheche.get(titolo); //
        if (bacheca != null && index >= 0 && index < bacheca.getToDos().size()) { //
            bacheca.rimuoviToDo(index); //
        }
    }

    public ToDo getToDoFromBacheca(Titolo titoloBacheca, int todoIndex) {
        Bacheca bacheca = bacheche.get(titoloBacheca); //
        if (bacheca != null && todoIndex >= 0 && todoIndex < bacheca.getToDos().size()) { //
            return bacheca.getToDos().get(todoIndex); //
        }
        return null;
    }

    public void modificaToDo(Titolo titoloBacheca, int todoIndex, String nuovoTitolo, String nuovaDescrizione) {
        Bacheca bacheca = bacheche.get(titoloBacheca); //
        if (bacheca != null) {
            ToDo toDoDaModificare = bacheca.getToDos().get(todoIndex); //
            if (toDoDaModificare != null) { //
                toDoDaModificare.setTitolo(nuovoTitolo); //
                toDoDaModificare.setDescrizione(nuovaDescrizione); //
            }
        }
    }

    // Nuovo metodo per spostare un ToDo tra bacheche (versione per GUI)
    public void spostaToDoGUI(Titolo bachecaOrigineTitolo, int todoIndex, Titolo bachecaDestinazioneTitolo) {
        Bacheca bachecaOrigine = bacheche.get(bachecaOrigineTitolo);
        Bacheca bachecaDestinazione = bacheche.get(bachecaDestinazioneTitolo);

        if (bachecaOrigine != null && bachecaDestinazione != null) {
            if (todoIndex >= 0 && todoIndex < bachecaOrigine.getToDos().size()) { //
                // Rimuovi il ToDo dalla bacheca di origine e ottienilo.
                // Il metodo List.remove(index) restituisce l'elemento rimosso.
                ToDo toDoDaSpostare = bachecaOrigine.getToDos().remove(todoIndex); //

                // Aggiungi il ToDo alla bacheca di destinazione
                bachecaDestinazione.aggiungiToDo(toDoDaSpostare); //
            } else {
                System.err.println("Indice ToDo non valido per lo spostamento.");
            }
        } else {
            System.err.println("Bacheca di origine o destinazione non valida per lo spostamento.");
        }
    }
}