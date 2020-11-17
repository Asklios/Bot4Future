package main.java.commands.administation;

import main.java.commands.ServerCommand;
import main.java.files.impl.GuildDatabaseSQLite;
import main.java.files.interfaces.GuildDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NewMuteRoleCommand implements ServerCommand {

    private GuildDatabase guildDatabase = new GuildDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        if (!member.hasPermission(Permission.ADMINISTRATOR)) return;
        if (message.getMentionedRoles().isEmpty()) {
            channel.sendMessage("Gib eine öffentliche Rolle an. Die neue Muterolle wird in allen Textkanälen mitlesen können auf welche diese Rolle Zugriff hat. " +
                    "`%newmuterole @publicRole`")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        Guild guild = channel.getGuild();
        Role publicRole = message.getMentionedRoles().get(0);
        List<TextChannel> guildChannels = guild.getTextChannels();
        ArrayList<TextChannel> publicChannels= new ArrayList<>();

        for (TextChannel t : guildChannels) {
            if (publicRole.hasAccess(t)) publicChannels.add(t);
        }

        EnumSet<Permission> allow = EnumSet.of(Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY);
        EnumSet<Permission> deny = EnumSet.of(Permission.CREATE_INSTANT_INVITE, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS,
                Permission.MANAGE_WEBHOOKS, Permission.MESSAGE_WRITE, Permission.MESSAGE_TTS, Permission.MESSAGE_MANAGE,Permission.MESSAGE_EMBED_LINKS,
                Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MENTION_EVERYONE, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_ADD_REACTION);

        guild.createRole().setName("mute").setColor(0x793da6).setPermissions(Permission.EMPTY_PERMISSIONS)
                .setPermissions(allow).queue(role -> {

            for (TextChannel t : publicChannels) {
                t.getManager().putPermissionOverride(role, allow, deny).queue();
            }

            this.guildDatabase.setMuteRole(guild, role);
            channel.sendMessage("Die Muterolle wurde auf " + role.getAsMention() + " festgelegt. Alle TextChannels auf welche " + publicRole.getName() +
                    " (" + publicRole.getId() + ") Zugreifen kann wurden eingestellt. Die neue Rolle kann in diesen mitlesen jedoch nicht schreiben.")
                    .queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
        });
    }
}
