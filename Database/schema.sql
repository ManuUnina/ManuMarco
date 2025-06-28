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
-- L'email viene usata come chiave primaria in quanto è un identificatore univoco per l'utente,
-- come suggerito dalla classe Utente.java e dal diagramma.
CREATE TABLE utente (
    email VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

-- Creazione della tabella per le bacheche.
-- Ogni bacheca ha un titolo univoco e una descrizione.
-- È collegata a un utente (il creatore) tramite una chiave esterna.
CREATE TABLE bacheca (
    titolo titolo_bacheca PRIMARY KEY,
    descrizione TEXT,
    -- Chiave esterna che collega la bacheca all'utente che l'ha creata.
    utente_email VARCHAR(255) NOT NULL,
    FOREIGN KEY (utente_email) REFERENCES utente(email) ON DELETE CASCADE
);

-- Creazione della tabella per i ToDo.
-- Ho utilizzato un ID seriale autoincrementante come chiave primaria per semplicità e robustezza,
-- sebbene il documento PDF suggerisca una chiave composita.
-- Ogni ToDo appartiene a una bacheca ed è creato da un autore.
CREATE TABLE todo (
    id SERIAL PRIMARY KEY,
    titolo VARCHAR(255) NOT NULL,
    descrizione TEXT,
    scadenza DATE,
    stato BOOLEAN DEFAULT FALSE,
    url VARCHAR(255),
    colore VARCHAR(7), -- Per memorizzare il colore in formato esadecimale (es. '#FFFFFF')
    -- Chiave esterna che collega il ToDo alla sua bacheca.
    bacheca_titolo titolo_bacheca NOT NULL,
    -- Chiave esterna che collega il ToDo all'utente autore.
    autore_email VARCHAR(255) NOT NULL,
    FOREIGN KEY (bacheca_titolo) REFERENCES bacheca(titolo) ON DELETE CASCADE,
    FOREIGN KEY (autore_email) REFERENCES utente(email) ON DELETE CASCADE
);

-- Creazione di una tabella di associazione per gestire la condivisione di un ToDo con più utenti.
-- Questo implementa la funzionalità della classe ListaUtenti.
-- La chiave primaria è composta dall'ID del ToDo e dall'email dell'utente per garantire che
-- ogni condivisione sia unica.
CREATE TABLE todo_utenti_condivisi (
    todo_id INT NOT NULL,
    utente_email VARCHAR(255) NOT NULL,
    PRIMARY KEY (todo_id, utente_email),
    FOREIGN KEY (todo_id) REFERENCES todo(id) ON DELETE CASCADE,
    FOREIGN KEY (utente_email) REFERENCES utente(email) ON DELETE CASCADE
);

-- Popolamento iniziale della tabella degli utenti come da file Utente.java
INSERT INTO utente (email, password) VALUES ('test@example.com', 'password123');
INSERT INTO utente (email, password) VALUES ('manu', 'cane123');