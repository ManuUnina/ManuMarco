package org.ToDo;

/**
 * Enumerazione che definisce i titoli predefiniti per le bacheche tematiche.
 * L'uso di un'enumerazione garantisce che i titoli delle bacheche siano
 * coerenti, limitati a un insieme fisso di valori e sicuri a livello di tipo (type-safe).
 * Questo previene errori di digitazione e rende il codice più leggibile e manutenibile.
 */
public enum Titolo {
        /**
         * Rappresenta la bacheca per le attività lavorative.
         */
        LAVORO,

        /**
         * Rappresenta la bacheca per le attività del tempo libero e personali.
         */
        TEMPO_LIBERO,

        /**
         * Rappresenta la bacheca per le attività legate all'università e allo studio.
         */
        UNIVERSITA
}