package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    // MODIFICA QUESTE CREDENZIALI CON LE TUE
    private static final String URL = "jdbc:postgresql://localhost:5432/todo_app_db"; // Assicurati che il nome del DB sia corretto
    private static final String USER = "postgres"; // o il tuo utente
    private static final String PASSWORD = "manu260603"; // la tua password

    public static Connection getConnection() {
        try {
            // Assicura che il driver sia caricato
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            // In un'applicazione reale, questo errore dovrebbe essere loggato
            // e gestito in modo più elegante.
            throw new RuntimeException("Errore durante la connessione al database", e);
        }
    }
}