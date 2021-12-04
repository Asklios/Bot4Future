package main.java.commands.server.user;

import lombok.Getter;
import lombok.Setter;
import main.java.commands.server.ServerCommand;
import main.java.files.impl.SelfRolesSQLite;
import main.java.files.interfaces.SelfRoles;
import net.dv8tion.jda.api.entities.*;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class IAmCommand implements ServerCommand {

    @Getter
    @Setter
    static HashMap<Long, HashMap<String, Long>> serverSelfRoles = new HashMap<>();

    private SelfRoles selfRoles = new SelfRolesSQLite();

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        //% iam <roleName>
        String[] messageSplit = message.getContentDisplay().split("\\s+");

        String searchString = "";
        for (int i = 1; i < messageSplit.length; i++) {
            if (searchString.equals("")) searchString = messageSplit[i];
            else searchString = searchString + " " + messageSplit[i];
        }

        long guildId = message.getGuild().getIdLong();
        HashMap<String, Long> guildRoles = serverSelfRoles.get(guildId);
        long roleId = 0;
        try {
            roleId = guildRoles.get(searchString.toLowerCase());
        } catch (NullPointerException e) {
            channel.sendMessage("Diese Rolle kannst du dir nicht selbst geben oder sie existiert nicht.")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        Guild guild = message.getGuild();
        Role role = guild.getRoleById(roleId);

        if (role == null) {
            channel.sendMessage("Die Rolle mit der id: " + roleId + " kann nicht mehr gefunden werden. " +
                            "Sie wird aus der Datenbank enfernt.")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            selfRoles.removeSelfRoleByRoleId(guildId, roleId);
            return;
        }

        if (member.getRoles().contains(role)) {
            channel.sendMessage("Du hast die Rolle " + role.getName() + " bereits!")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        guild.addRoleToMember(member, role).queue();
        channel.sendMessage(member.getAsMention() + " du hast die Rolle `" + role.getName() + "` erhalten.").queue();
    }
}
