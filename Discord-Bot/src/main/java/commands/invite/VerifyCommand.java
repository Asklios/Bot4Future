package main.java.commands.invite;

import main.java.commands.ServerCommand;
import main.java.files.impl.GuildDatabaseSQLite;
import main.java.files.impl.InviteDatabaseSQLite;
import main.java.files.interfaces.GuildDatabase;
import main.java.files.interfaces.InviteDatabase;
import net.dv8tion.jda.api.entities.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class VerifyCommand implements ServerCommand {

    GuildDatabase guildDatabase = new GuildDatabaseSQLite();
    InviteDatabase inviteDatabase = new InviteDatabaseSQLite();

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        Role highestBotRole = message.getGuild().getSelfMember().getRoles().get(0);
        Role verifiableRole = this.guildDatabase.getVerifyRole(channel.getGuild());
        Role specialRole = this.guildDatabase.getSpecialRole(channel.getGuild());

        try {
            if (specialRole == null) {
                channel.sendMessage("Die aktuelle Special-Code-Rolle ist nicht festgelegt. ```%specialcoderole @role```").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                return;
            }

            if (hasRole(member, specialRole)) {

                if (highestBotRole.canInteract(verifiableRole)) {
                    giveVerifiableRole(member, channel, message, verifiableRole);
                } else {
                    channel.sendMessage("Diese Rolle kann nicht verwendet werden, da sie höher als die Bot-Rolle ist.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                }
            }
            else {
                channel.sendMessage("Du benötigst die Rolle " + specialRole.getName() + " um diesen Command zu nutzen.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Cought Exception: NumberFormatException (VerifyCommand.java - performCommand)");
        }
    }

    private void giveVerifiableRole(Member member, TextChannel channel, Message message, Role verifiableRole) {
        try {
            if (verifiableRole == null) {
                channel.sendMessage("Die aktuelle VerifiableRole ist nicht festgelegt.\n```%verifiablerole @role```").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                return;
            }

            List<Member> members = message.getMentionedMembers();

            for (Member m : members) {
                Guild guild = m.getGuild();
                channel.sendTyping().queue();

                if (!hasRole(m, verifiableRole)) {
                    guild.addRoleToMember(m.getIdLong(), verifiableRole).queue();
                    this.inviteDatabase.saveVerified(member, m);
                    channel.sendMessage(m.getAsMention() + " hat die Rolle " + verifiableRole.getName() + " erhalten.").queue(me -> me.delete().queueAfter(10,TimeUnit.SECONDS));
                } else {
                    channel.sendMessage(m.getAsMention() + " hat die Rolle " + verifiableRole.getName() + " schon.").queue(me -> me.delete().queueAfter(10,TimeUnit.SECONDS));
                }
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Cought Exception: NumberFormatException (SpecialInviteCodeCommand.java - performCommand)");
        }
    }

    private boolean hasRole(Member member, Role specialRole) {
        List<Role> memberRoles = member.getRoles();

        for (Role r : memberRoles) { // überprüfen ob der Nutzer die specialCodeRole besitzt
            if (r == specialRole) {
                return true;
            }
        }
        return false;
    }
}


