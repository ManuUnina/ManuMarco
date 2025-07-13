package controller;

import dao.BachecaDAO;
import dao.ListaUtentiDAO;
import dao.ToDoDAO;
import dao.UtenteDAO;
import org.ToDo.Bacheca;
import org.ToDo.ListaUtenti;
import org.ToDo.Titolo;
import org.ToDo.ToDo;
import org.ToDo.Utente;
import gui.View;

import javax.swing.*;
import java.awt.Color;
import java.time.LocalDate;
import java.util.Map;

public class Controller {
    private View view;
    private Map<Titolo, Bacheca> bacheche;
    private Bacheca bachecaCondivisi;
    private final UtenteDAO utenteDAO;
    private final BachecaDAO bachecaDAO;
    private final ToDoDAO toDoDAO;
    private final ListaUtentiDAO listaUtentiDAO;
    private Utente utenteCorrente;
    private ListaUtenti contatti;

    public Controller() {
        this.utenteDAO = new UtenteDAO();
        this.bachecaDAO = new BachecaDAO();
        this.toDoDAO = new ToDoDAO();
        this.listaUtentiDAO = new ListaUtentiDAO();
    }

    public void init() {
        while (true) {
            boolean autenticazioneRiuscita = gestisciAutenticazione();
            if (autenticazioneRiuscita) {
                popolaDatiUtente();
                SwingUtilities.invokeLater(() -> {
                    view = new View(this);
                    view.setVisible(true);
                });
                return;
            } else {
                int sceltaUscita = JOptionPane.showConfirmDialog(null, "Accesso non completato. Desideri uscire?", "Esci o Riprova?", JOptionPane.YES_NO_OPTION);
                if (sceltaUscita == JOptionPane.YES_OPTION) {
                    System.exit(0);
                    return;
                }
            }
        }
    }

    private void popolaDatiUtente() {
        this.bacheche = bachecaDAO.findAllForUser(this.utenteCorrente.getEmail());
        if (this.bacheche.isEmpty()) {
            for (Titolo t : Titolo.values()) {
                Bacheca nuovaBacheca = new Bacheca(t, "Descrizione per " + t.name(), this.utenteCorrente.getEmail());
                bachecaDAO.save(nuovaBacheca);
                this.bacheche.put(t, nuovaBacheca);
            }
        }
        for (Bacheca b : this.bacheche.values()) {
            b.setToDos(toDoDAO.findByBacheca(b.getTitolo(), this.utenteCorrente.getEmail()));
        }
        this.bachecaCondivisi = new Bacheca(null, "ToDo condivisi da altri utenti", this.utenteCorrente.getEmail());
        this.bachecaCondivisi.setToDos(toDoDAO.findSharedWithUser(this.utenteCorrente.getEmail()));
        this.contatti = listaUtentiDAO.getContattiForUser(this.utenteCorrente.getEmail());
    }

    private boolean gestisciAutenticazione() {
        String[] opzioni = {"Login", "Registrati"};
        int scelta = JOptionPane.showOptionDialog(null, "Benvenuto! Cosa desideri fare?", "ToDo App", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opzioni, opzioni[0]);
        if (scelta == 0) return eseguiLoginDialog();
        if (scelta == 1) {
            Utente nuovoUtente = eseguiRegistrazioneDialog();
            if (nuovoUtente != null) {
                JOptionPane.showMessageDialog(null, "Registrazione completata!", "Registrazione Riuscita", JOptionPane.INFORMATION_MESSAGE);
                this.utenteCorrente = nuovoUtente;
                return true;
            }
        }
        return false;
    }

    private boolean eseguiLoginDialog() {
        JTextField emailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        Object[] messaggio = {"Email:", emailField, "Password:", passwordField};
        int opzione = JOptionPane.showConfirmDialog(null, messaggio, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (opzione == JOptionPane.OK_OPTION) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            Utente utente = utenteDAO.findByEmailAndPassword(email, password);
            if (utente != null) {
                this.utenteCorrente = utente;
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Credenziali non valide. Riprova.", "Errore di Login", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    private Utente eseguiRegistrazioneDialog() {
        JTextField emailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confermaPasswordField = new JPasswordField(20);
        Object[] messaggio = {"Email:", emailField, "Password (min. 3 caratteri):", passwordField, "Conferma Password:", confermaPasswordField};
        int opzione = JOptionPane.showConfirmDialog(null, messaggio, "Registrazione", JOptionPane.OK_CANCEL_OPTION);
        if (opzione == JOptionPane.OK_OPTION) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confermaPassword = new String(confermaPasswordField.getPassword());

            if (email.isEmpty() || !email.contains("@") || password.length() < 3 || !password.equals(confermaPassword)) {
                JOptionPane.showMessageDialog(null, "Dati non validi. Controlla i campi e riprova.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (utenteDAO.isEmailRegistered(email)) {
                JOptionPane.showMessageDialog(null, "L'email '" + email + "' è già in uso.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            Utente nuovoUtente = new Utente(email, password);
            if (utenteDAO.registraNuovoUtente(nuovoUtente)) {
                return nuovoUtente;
            } else {
                JOptionPane.showMessageDialog(null, "Errore durante il salvataggio nel database.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    public void eseguiLogout() {
        this.utenteCorrente = null;
        if (this.view != null) {
            this.view.dispose();
        }
        new Controller().init();
    }

    public Utente getUtenteCorrente() { return this.utenteCorrente; }
    public Map<Titolo, Bacheca> getBacheche() { return this.bacheche; }
    public Bacheca getBachecaCondivisi() { return this.bachecaCondivisi; }
    public ListaUtenti getContatti() { return this.contatti; }

    public void aggiungiContatto(String email) {
        if (!utenteDAO.isEmailRegistered(email)) {
            JOptionPane.showMessageDialog(null, "Nessun utente registrato con l'email: " + email, "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (utenteCorrente.getEmail().equals(email)) {
            JOptionPane.showMessageDialog(null, "Non puoi aggiungere te stesso ai contatti.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (contatti.getLista().contains(email)) {
            JOptionPane.showMessageDialog(null, "Questo utente è già nei tuoi contatti.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        listaUtentiDAO.aggiungiContatto(utenteCorrente.getEmail(), email);
        contatti.aggiungiUtente(email);
        JOptionPane.showMessageDialog(null, "Contatto aggiunto con successo.", "Successo", JOptionPane.INFORMATION_MESSAGE);
        view.refreshContatti();
    }

    public void rimuoviContatto(String email) {
        listaUtentiDAO.rimuoviContatto(utenteCorrente.getEmail(), email);
        contatti.rimuovi(email);
        JOptionPane.showMessageDialog(null, "Contatto rimosso con successo.", "Successo", JOptionPane.INFORMATION_MESSAGE);
        view.refreshContatti();
    }

    public void aggiungiToDo(Titolo titolo, ToDo todo) {
        toDoDAO.save(todo);
        this.bacheche.get(titolo).aggiungiToDo(todo);
        view.refreshToDoList();
    }

    public void rimuoviToDo(Titolo titolo, ToDo todo) {
        Bacheca bacheca = this.bacheche.get(titolo);
        if (bacheca != null) {
            toDoDAO.delete(todo.getId());
            bacheca.getToDos().remove(todo);
            view.refreshToDoList();
        }
    }

    public void toggleToDoStatus(Titolo bachecaTitolo, ToDo todoToToggle, boolean newStatus) {
        if (todoToToggle != null) {
            todoToToggle.setStato(newStatus);
            toDoDAO.update(todoToToggle);
            view.refreshToDoList();
        }
    }

    public void modificaDescrizioneBacheca(Titolo titoloBacheca, String nuovaDescrizione) {
        bachecaDAO.updateDescrizione(titoloBacheca, nuovaDescrizione, this.utenteCorrente.getEmail());
        this.bacheche.get(titoloBacheca).setDescrizione(nuovaDescrizione);
        view.refreshToDoList();
    }

    public void modificaToDo(Titolo titoloBacheca, ToDo toDoDaModificare, String nuovoTitolo, String nuovaDescrizione, LocalDate nuovaScadenza, boolean nuovoStato, Color nuovoColore, String nuovoUrl, String nuovaPosizione, byte[] nuovaImmagine) {
        Bacheca bacheca = this.bacheche.get(titoloBacheca);
        if (bacheca != null && toDoDaModificare != null) {
            toDoDaModificare.setTitolo(nuovoTitolo);
            toDoDaModificare.setDescrizione(nuovaDescrizione);
            toDoDaModificare.setScadenza(nuovaScadenza);
            toDoDaModificare.setStato(nuovoStato);
            toDoDaModificare.setColore(nuovoColore);
            toDoDaModificare.setUrl(nuovoUrl);
            toDoDaModificare.setPosizione(nuovaPosizione);
            toDoDaModificare.setImmagine(nuovaImmagine);
            toDoDAO.update(toDoDaModificare);
            view.refreshToDoList();
        }
    }

    public void spostaToDoGUI(Titolo bachecaOrigineTitolo, ToDo toDoDaSpostare, Titolo bachecaDestinazioneTitolo) {
        Bacheca bachecaOrigine = this.bacheche.get(bachecaOrigineTitolo);
        if (bachecaOrigine != null && toDoDaSpostare != null) {
            toDoDaSpostare.setBachecaTitolo(bachecaDestinazioneTitolo);
            toDoDAO.update(toDoDaSpostare);
            bachecaOrigine.getToDos().remove(toDoDaSpostare);
            this.bacheche.get(bachecaDestinazioneTitolo).aggiungiToDo(toDoDaSpostare);
            view.refreshToDoList();
        }
    }

    public void condividiToDo(ToDo todo, String emailToShareWith) {
        if (!utenteDAO.isEmailRegistered(emailToShareWith)) {
            JOptionPane.showMessageDialog(null, "Nessun utente registrato con l'email: " + emailToShareWith, "Errore Condivisione", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (todo.getAutoreEmail().equals(emailToShareWith)) {
            JOptionPane.showMessageDialog(null, "Non puoi condividere un ToDo con te stesso.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        todo.getListaUtenti().aggiungiUtente(emailToShareWith);
        listaUtentiDAO.addUserToSharedList(todo.getId(), emailToShareWith);
        JOptionPane.showMessageDialog(null, "ToDo condiviso con successo con " + emailToShareWith, "Condivisione Riuscita", JOptionPane.INFORMATION_MESSAGE);
    }
}