-- Istruzione per eliminare le tabelle esistenti se necessario, per una facile reinizializzazione.
DROP TABLE IF EXISTS todo_utenti_condivisi;
DROP TABLE IF EXISTS todo;
DROP TABLE IF EXISTS bacheca;
DROP TABLE IF EXISTS utente;
DROP TYPE IF EXISTS titolo_bacheca;

-- Creazione di un tipo ENUM per i titoli delle bacheche, come definito nel file Titolo.java
-- Questo garantisce che i titoli siano standardizzati e coerenti.
CREATE TYPE titolo_bacheca AS ENUM ('LAVORO', 'TEMPO_LIBERO', 'UNIVERSITA');

-- Creazione della tabella per gli utenti.
-- L'email viene usata come chiave primaria in quanto è un identificatore univoco per l'utente.
CREATE TABLE utente (
                        email VARCHAR(255) PRIMARY KEY,
                        password VARCHAR(255) NOT NULL
);

-- Creazione della tabella per le bacheche.
-- La chiave primaria è composita (titolo, utente_email) per permettere a più utenti
-- di avere bacheche con lo stesso titolo (es. sia utente A che B possono avere una bacheca 'LAVORO').
CREATE TABLE bacheca (
                         titolo titolo_bacheca NOT NULL,
                         descrizione TEXT,
                         utente_email VARCHAR(255) NOT NULL,
                         PRIMARY KEY (titolo, utente_email),
                         FOREIGN KEY (utente_email) REFERENCES utente(email) ON DELETE CASCADE
);

-- Creazione della tabella per i ToDo.
-- Un ID seriale autoincrementante è usato come chiave primaria per semplicità.
CREATE TABLE todo (
                      id SERIAL PRIMARY KEY,
                      titolo VARCHAR(255) NOT NULL,
                      descrizione TEXT,
                      scadenza DATE,
                      stato BOOLEAN DEFAULT FALSE,
                      url VARCHAR(255),
                      colore VARCHAR(7), -- Per memorizzare il colore in formato esadecimale (es. '#FFFFFF')
                      immagine BYTEA,    -- NUOVA COLONNA per i dati binari dell'immagine
    -- Chiavi esterne
                      bacheca_titolo titolo_bacheca NOT NULL,
                      autore_email VARCHAR(255) NOT NULL,
    -- La foreign key ora referenzia correttamente la chiave primaria composita di 'bacheca'
                      FOREIGN KEY (bacheca_titolo, autore_email) REFERENCES bacheca(titolo, utente_email) ON DELETE CASCADE,
                      FOREIGN KEY (autore_email) REFERENCES utente(email) ON DELETE CASCADE
);

-- Tabella di associazione per gestire la condivisione di un ToDo con più utenti.
CREATE TABLE todo_utenti_condivisi (
                                       todo_id INT NOT NULL,
                                       utente_email VARCHAR(255) NOT NULL,
                                       PRIMARY KEY (todo_id, utente_email),
                                       FOREIGN KEY (todo_id) REFERENCES todo(id) ON DELETE CASCADE,
                                       FOREIGN KEY (utente_email) REFERENCES utente(email) ON DELETE CASCADE
);

-- Popolamento iniziale
INSERT INTO utente (email, password) VALUES ('test@example.com', 'password123');
INSERT INTO utente (email, password) VALUES ('manu', 'cane123');