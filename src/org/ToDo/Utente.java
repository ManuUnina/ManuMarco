package org.ToDo;

public class Utente {
    protected String email;
    protected String password;

    public Utente(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public boolean verificaCredenziali(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }

    public String getEmail() {
        return email;
    }
}

