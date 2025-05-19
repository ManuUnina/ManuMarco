package org.ToDo;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;

public class Utente {
    // Campi di istanza (per un singolo utente)
    protected String email;
    protected String password;

    // Campi statici per la gestione globale degli utenti
    private static List<Utente> listaGlobaleUtentiRegistrati = new ArrayList<>();
    private static Utente utenteCorrenteAutenticato = null;

    // Costruttore di istanza
    public Utente(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Metodo di istanza per verificare le credenziali di questo utente
    public boolean verificaCredenziali(String emailInput, String passwordInput) {
        return this.email.equals(emailInput) && this.password.equals(passwordInput);
    }

    // Getter di istanza
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public static void popolaUtentiIniziali() {
        // Assicurati che la lista sia pulita se chiami questo metodo più volte (es. per test)
        if (listaGlobaleUtentiRegistrati.isEmpty()) {
            listaGlobaleUtentiRegistrati.add(new Utente("test@example.com", "password123"));
            listaGlobaleUtentiRegistrati.add(new Utente("manu", "cane123"));
        }
    }

    public static boolean autenticaUtente(String emailInput, String passwordInput) {
        for (Utente utente : listaGlobaleUtentiRegistrati) {
            if (utente.verificaCredenziali(emailInput, passwordInput)) {
                utenteCorrenteAutenticato = utente; // Imposta l'utente corrente
                return true;
            }
        }
        utenteCorrenteAutenticato = null; // Nessun utente trovato o credenziali errate
        return false;
    }

    public static boolean registraNuovoUtente(String emailInput, String passwordInput) {
        for (Utente utente : listaGlobaleUtentiRegistrati) {
            if (utente.getEmail().equalsIgnoreCase(emailInput)) {
                // Il messaggio di errore viene ora gestito dal Controller che chiama questo metodo
                // JOptionPane.showMessageDialog(null, "L'email '" + emailInput + "' è già registrata.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE);
                return false; // Email già esistente
            }
        }
        Utente nuovoUtente = new Utente(emailInput, passwordInput);
        listaGlobaleUtentiRegistrati.add(nuovoUtente);
        return true; // Registrazione avvenuta con successo
    }

    public static Utente getUtenteCorrenteAutenticato() {
        return utenteCorrenteAutenticato;
    }

    public static void logout() { // Metodo opzionale per il logout
        utenteCorrenteAutenticato = null;
    }
}
