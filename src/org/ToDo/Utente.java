package org.ToDo;

public class Utente {
    private String email;
    private String password;

    public Utente(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}