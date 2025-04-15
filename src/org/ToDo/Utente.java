package org.ToDo;
/*
public class Utente {
    public String email;
    public String password;


    public Utente (String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String stampa(){
        return "l'email inserita è:" + email + "\nla password è:" + password;
    }
}*/

public class Utente {
    private String email;
    private String password;

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

