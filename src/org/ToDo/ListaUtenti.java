package org.ToDo;

import java.util.ArrayList;

public class ListaUtenti {
    public String Autore;
    public ArrayList<String> Lista;



    public ListaUtenti(String Autore, ArrayList<String> Lista) {
        this.Autore=Autore;
        this.Lista=new ArrayList<String>(Lista);
    }

    public ArrayList<String> getLista(){
        return Lista;
    }

    public String getAutore(){
        return Autore;
    }

    public void setAutore(String Autore){
        this.Autore = Autore;
    }

    public void aggiungiUtente(String email) {
        if (!Lista.contains(email)) {
            Lista.add(email);
        }
    }

    public void rimuovi(String utente){
        Lista.remove(utente);
    }

    public int dimensione (ListaUtenti l){
        return Lista.size();
    }

    @Override
    public String toString() {
        return "Autore: " + Autore + "\nCondiviso con: " + Lista.toString();
    }
}