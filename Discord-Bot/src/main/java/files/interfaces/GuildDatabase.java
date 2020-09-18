package main.java.files.interfaces;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

public interface GuildDatabase {

    void startUpEntries(Guild guild) throws NullPointerException;

    void setMuteRole(Guild guild, Role role) throws NullPointerException;
    Role getMuteRole(Guild guild);

    void setSpecialRole(Guild guild, Role role) throws NullPointerException;
    Role getSpecialRole(Guild guild);
    void setVerifyRole(Guild guild, Role role) throws NullPointerException;
    Role getVerifyRole(Guild guild);
    void setSpecialCode(Guild guild, String code) throws NullPointerException;
    String getSpecialCode(Guild guild);

}
