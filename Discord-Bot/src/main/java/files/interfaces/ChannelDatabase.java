package main.java.files.interfaces;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public interface ChannelDatabase {

    void startUpEntries(Guild guild) throws NullPointerException;

    void saveAuditChannel (TextChannel textChannel) throws NullPointerException;
    TextChannel getAuditChannel (Guild guild);

    void savePnChannel (TextChannel textChannel) throws NullPointerException;
    TextChannel getPnChannel (Guild guild);

}
