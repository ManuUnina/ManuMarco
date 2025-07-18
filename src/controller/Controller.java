package controller;

import dao.*;
import implementazioniPostgresDAO.*;
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

/**
 * Il Controller gestisce la logica di business e funge da intermediario
 * tra la View (interfaccia utente) e il Model (le classi DAO e di dominio).
 * È responsabile di inizializzare l'applicazione, gestire l'autenticazione,
 * coordinare le operazioni sui dati e aggiornare la vista di conseguenza.
 */
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

    /**
     * Costruttore del Controller. Inizializza le implementazioni dei DAO
     * per l'accesso ai dati del database.
     */
    public Controller() {
        this.utenteDAO = new UtenteDAOImpl();
        this.bachecaDAO = new BachecaDAOImpl();
        this.toDoDAO = new ToDoDAOImpl();
        this.listaUtentiDAO = new ListaUtentiDAOImpl();
    }

    /**
     * Inizializza il flusso principale dell'applicazione.
     * Gestisce il ciclo di autenticazione e, in caso di successo,
     * popola i dati dell'utente e avvia l'interfaccia grafica (View).
     * Se l'autenticazione fallisce, permette all'utente di riprovare o uscire.
     */
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

    /**
     * Carica tutti i dati necessari per l'utente corrente dopo un login riuscito.
     * Include il recupero delle bacheche, dei ToDo associati, dei ToDo condivisi
     * e della lista contatti. Se l'utente non ha bacheche, le crea.
     */
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

    /**
     * Gestisce il processo di autenticazione iniziale, offrendo all'utente
     * la scelta tra Login e Registrazione.
     *
     * @return true se l'autenticazione (login o registrazione) ha successo, false altrimenti.
     */
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

    /**
     * Mostra una finestra di dialogo per il login dell'utente.
     *
     * @return true se il login ha successo, false altrimenti.
     */
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

    /**
     * Mostra una finestra di dialogo per la registrazione di un nuovo utente.
     * Esegue controlli di validazione sui dati inseriti.
     *
     * @return L'oggetto {@link Utente} appena creato se la registrazione ha successo, altrimenti null.
     */
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

    /**
     * Esegue il logout dell'utente corrente, chiude la vista attuale
     * e reinizializza il controller per mostrare nuovamente la schermata di autenticazione.
     */
    public void eseguiLogout() {
        this.utenteCorrente = null;
        if (this.view != null) {
            this.view.dispose();
        }
        new Controller().init();
    }

    /** @return L'utente attualmente autenticato. */
    public Utente getUtenteCorrente() { return this.utenteCorrente; }
    /** @return La mappa delle bacheche dell'utente corrente. */
    public Map<Titolo, Bacheca> getBacheche() { return this.bacheche; }
    /** @return La bacheca speciale contenente i ToDo condivisi con l'utente. */
    public Bacheca getBachecaCondivisi() { return this.bachecaCondivisi; }
    /** @return La lista dei contatti dell'utente corrente. */
    public ListaUtenti getContatti() { return this.contatti; }

    /**
     * Aggiunge un contatto alla lista dell'utente corrente, dopo aver verificato
     * che l'utente esista e non sia già presente o l'utente stesso.
     *
     * @param email L'email del contatto da aggiungere.
     */
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

    /**
     * Rimuove un contatto dalla lista dell'utente corrente.
     *
     * @param email L'email del contatto da rimuovere.
     */
    public void rimuoviContatto(String email) {
        listaUtentiDAO.rimuoviContatto(utenteCorrente.getEmail(), email);
        contatti.rimuovi(email);
        JOptionPane.showMessageDialog(null, "Contatto rimosso con successo.", "Successo", JOptionPane.INFORMATION_MESSAGE);
        view.refreshContatti();
    }

    /**
     * Aggiunge un nuovo ToDo a una bacheca specifica.
     *
     * @param titolo Il titolo della bacheca a cui aggiungere il ToDo.
     * @param todo L'oggetto {@link ToDo} da aggiungere.
     */
    public void aggiungiToDo(Titolo titolo, ToDo todo) {
        toDoDAO.save(todo);
        this.bacheche.get(titolo).aggiungiToDo(todo);
        view.refreshToDoList();
    }

    /**
     * Rimuove un ToDo da una bacheca.
     *
     * @param titolo Il titolo della bacheca da cui rimuovere il ToDo.
     * @param todo L'oggetto {@link ToDo} da rimuovere.
     */
    public void rimuoviToDo(Titolo titolo, ToDo todo) {
        Bacheca bacheca = this.bacheche.get(titolo);
        if (bacheca != null) {
            toDoDAO.delete(todo.getId());
            bacheca.getToDos().remove(todo);
            view.refreshToDoList();
        }
    }

    /**
     * Cambia lo stato di completamento di un ToDo (completato/incompleto).
     *
     * @param bachecaTitolo Il titolo della bacheca del ToDo.
     * @param todoToToggle Il ToDo da modificare.
     * @param newStatus Il nuovo stato booleano (true per completato).
     */
    public void toggleToDoStatus(Titolo bachecaTitolo, ToDo todoToToggle, boolean newStatus) {
        if (todoToToggle != null) {
            todoToToggle.setStato(newStatus);
            toDoDAO.update(todoToToggle);
            view.refreshToDoList();
        }
    }

    /**
     * Modifica la descrizione di una bacheca.
     *
     * @param titoloBacheca Il titolo della bacheca da modificare.
     * @param nuovaDescrizione La nuova descrizione.
     */
    public void modificaDescrizioneBacheca(Titolo titoloBacheca, String nuovaDescrizione) {
        bachecaDAO.updateDescrizione(titoloBacheca, nuovaDescrizione, this.utenteCorrente.getEmail());
        this.bacheche.get(titoloBacheca).setDescrizione(nuovaDescrizione);
        view.refreshToDoList();
    }

    /**
     * Modifica tutti gli attributi di un ToDo esistente.
     *
     * @param titoloBacheca Il titolo della bacheca di appartenenza.
     * @param toDoDaModificare L'oggetto ToDo originale.
     * @param nuovoTitolo Il nuovo titolo.
     * @param nuovaDescrizione La nuova descrizione.
     * @param nuovaScadenza La nuova data di scadenza.
     * @param nuovoStato Il nuovo stato di completamento.
     * @param nuovoColore Il nuovo colore.
     * @param nuovoUrl Il nuovo URL.
     * @param nuovaPosizione La nuova posizione.
     * @param nuovaImmagine La nuova immagine come array di byte.
     */
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

    /**
     * Sposta un ToDo da una bacheca all'altra.
     *
     * @param bachecaOrigineTitolo Il titolo della bacheca di partenza.
     * @param toDoDaSpostare Il ToDo da spostare.
     * @param bachecaDestinazioneTitolo Il titolo della bacheca di destinazione.
     */
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

    /**
     * Condivide un ToDo con un altro utente.
     *
     * @param todo Il ToDo da condividere.
     * @param emailToShareWith L'email dell'utente con cui condividere il ToDo.
     */
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