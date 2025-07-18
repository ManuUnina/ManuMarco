package org.ToDo;

/**
 * Rappresenta un utente dell'applicazione.
 * Questa classe contiene le informazioni fondamentali per l'identificazione
 * e l'autenticazione di un utente, ovvero l'email e la password.
 * È una classe modello semplice (POJO - Plain Old Java Object) utilizzata
 * per trasportare i dati dell'utente tra i vari layer dell'applicazione.
 */
public class Utente {
    /**
     * L'indirizzo email dell'utente, utilizzato come identificatore univoco (login).
     */
    private String email;
    /**
     * La password dell'utente, utilizzata per l'autenticazione.
     */
    private String password;

    /**
     * Costruttore per creare una nuova istanza di Utente.
     *
     * @param email L'email dell'utente.
     * @param password La password dell'utente.
     */
    public Utente(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Restituisce l'email dell'utente.
     *
     * @return una stringa che rappresenta l'email dell'utente.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Restituisce la password dell'utente.
     *
     * @return una stringa che rappresenta la password dell'utente.
     */
    public String getPassword() {
        return password;
    }
}