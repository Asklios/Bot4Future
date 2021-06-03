package main.java.files.interfaces;

import main.java.helper.api.LocalGroups;

public interface StrikeUpdateDatabase {
    /**
     * Gibt das letzte bearbeitete Update eines Streiks wieder. Dies wird genutzt, damit die Streikinfos nicht mehrmals an die
     * Abonenten gesendet wird
     * @param strikeId die ID des Streiks
     * @return unix timestamp des letzten bearbeiteten Updates
     */
    Long getLastHandledUpdate(Long strikeId);

    /**
     * Setze das Letzte Update eines Streiks
     * @param strikeId mit der ID des Streiks
     * @param timestamp und dem Unix-Timestamp
     */
    void setLastHandledUpdate(Long strikeId, Long timestamp);

}
