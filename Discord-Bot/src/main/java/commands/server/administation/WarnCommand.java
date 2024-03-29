package main.java.commands.server.administation;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.UserRecordsDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.UserRecordsDatabase;
import main.java.util.MsgCreator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class WarnCommand implements ServerCommand {

    private ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();
    private UserRecordsDatabase userRecordsDatabase = new UserRecordsDatabaseSQLite();

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {

        if (!member.hasPermission(Permission.KICK_MEMBERS)) {
            return;
        }

        // %warn @member <reason>
        Guild guild = channel.getGuild();
        Member targetMember = null;
        String[] messageSplit = message.getContentRaw().split("\\s+");
        int messageLength = messageSplit.length;

        String reason = " ";
        for (int i = messageLength - 1; i > 1; i--) {
            reason = String.join(messageSplit[i], " ", reason);
        }

        try {
            targetMember = message.getMentionedMembers().get(0);
        } catch (IndexOutOfBoundsException e) {
            try {
                targetMember = guild.retrieveMemberById(messageSplit[1]).complete();
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException f) {
                channel.sendMessage("Es wurde kein User erwähnt. `%warn @user <reason>`").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
                return;
            }
        }

        if (targetMember == null) {
            channel.sendMessage("Es wurde kein User erwähnt. `%warn @user <reason>`").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        User targetUser = targetMember.getUser();

        if (messageSplit.length < 2) {
            channel.sendMessage("`%warn @user <reason>`").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        if (reason.equals(" ")) {
            channel.sendMessage("Es wurde keine Begründung angegeben. `%warn @user <reason>`").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        userRecordsDatabase.addRecord(targetMember.getIdLong(), System.currentTimeMillis(), 0,
                "warning", guild.getIdLong(), reason, "active");

        TextChannel audit = this.channelDatabase.getAuditChannel(guild);

        EmbedBuilder builder = new EmbedBuilder();
        try {
            builder.setTitle(":cop: Nutzer verwarnt");
            builder.setTimestamp(OffsetDateTime.now());
            builder.setColor(0x1da64a); // FFF_grün
            builder.setThumbnail(targetUser.getAvatarUrl() == null ? targetUser.getDefaultAvatarUrl() : targetUser.getAvatarUrl()); // wenn AvatarUrl = null ist wird der DefaultAvatar vewendet
            builder.setFooter("by " + message.getAuthor().getName());
            builder.addField("Name: ", targetUser.getAsMention(), false);
            builder.addField("ID: ", targetUser.getId(), false);
            builder.addField(":page_facing_up:Begründung:", reason, false);
        } catch (IllegalArgumentException e) {
            channel.sendMessage(message.getAuthor() + "Es konnte keine Auditnachricht geschickt werden, vermutlich wurde ein Zeichenlimit überschritten.")
                    .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
        }

        channel.sendMessage(targetMember.getEffectiveName() + " wurde verwarnt. Begründung: " + reason).queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));

        if (audit == null) {
            channel.sendMessage("Es wurde noch kein Audit-Channel festgelegt `%audit #channel`").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        audit.sendMessage(MsgCreator.of(builder)).queue();
    }
}
