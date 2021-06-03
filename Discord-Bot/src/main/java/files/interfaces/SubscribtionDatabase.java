package main.java.files.interfaces;

import java.util.List;
import java.util.Map;

public interface SubscribtionDatabase {
    /**
     * Gibt die Streik-Updates-Datenbank zurück
     * @return die Datenbank
     */
    StrikeUpdateDatabase getUpdateDatabase();
    /**
     * Gibt die IDs aller Ortsgruppen an, die ein Nutzer aboniert hat.
     *
     * @param id User ID
     * @return Liste mit allen OG-Ids
     */
    List<Long> getSubscribtionsOfUser(String id);

    /**
     * Aboniere eine Ortsgruppe..
     *
     * @param userId ..für den Nutzer mit angegebener ID
     * @param ogId   ..welche diese ID besitzt.
     */
    void subscribe(String userId, Long ogId);

    /**
     * Unaboniere eine Ortsgruppe..
     *
     * @param userId ..für den Nutzer mit angegebener ID
     * @param ogId   ..welche diese ID besitzt.
     */
    void unsubscribe(String userId, Long ogId);

    /**
     * Fordere alle Abonements an.
     * @return Map<OG-ID, List<UserID>>
     */
    Map<Long, List<String>> getAllSubscribtions();
}
