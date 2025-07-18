package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Una classe di utilità per la gestione della connessione al database.
 * Fornisce un metodo statico per ottenere una connessione al database PostgreSQL.
 * Le credenziali di connessione (URL, utente, password) sono definite come costanti private.
 */
public class ConnectionFactory {
    /**
     * L'URL di connessione al database PostgreSQL.
     * Include l'host, la porta e il nome del database.
     */
    private static final String URL = "jdbc:postgresql://localhost:5432/todo_app_db";
    /**
     * Il nome utente per l'accesso al database.
     */
    private static final String USER = "postgres";
    /**
     * La password per l'accesso al database.
     */
    private static final String PASSWORD = "manu260603";

    /**
     * Stabilisce e restituisce una connessione al database.
     * Il metodo assicura che il driver JDBC di PostgreSQL sia caricato e utilizza
     * le credenziali definite in questa classe per creare la connessione.
     *
     * @return un oggetto {@link Connection} che rappresenta la connessione al database.
     * @throws RuntimeException se si verifica un errore durante il caricamento del driver
     * o se non è possibile stabilire una connessione (incapsulando
     * {@link ClassNotFoundException} o {@link SQLException}).
     */
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