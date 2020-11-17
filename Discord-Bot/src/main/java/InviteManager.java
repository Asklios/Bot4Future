package main.java;

import main.java.files.impl.ChannelDatabaseSQLite;
import main.java.files.impl.GuildDatabaseSQLite;
import main.java.files.impl.InviteDatabaseSQLite;
import main.java.files.interfaces.ChannelDatabase;
import main.java.files.interfaces.GuildDatabase;
import main.java.files.interfaces.InviteDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.time.OffsetDateTime;
import java.util.List;

public class InviteManager {

    Guild guild;
    Long guildID;
    String specialInviteCode;
    int inviteCount;
    GuildDatabase guildDatabase = new GuildDatabaseSQLite();
    InviteDatabase inviteDatabase = new InviteDatabaseSQLite();
    ChannelDatabase channelDatabase = new ChannelDatabaseSQLite();

    public InviteManager(Guild guild, String specialInviteCode) {

        this.guild = guild;
        this.specialInviteCode = specialInviteCode;

        guildID = guild.getIdLong();

        saveInviteCount();
    }

    private void saveInviteCount() {

        List<Invite> invites = guild.retrieveInvites().complete();
        for (int i = 0; i < invites.size(); i++) {

            if (invites.get(i).getCode().equals(specialInviteCode)) {
                inviteCount = invites.get(i).getUses();
            }
        }
    }

    public void checkNewMember(Member member) {
        try {
            List<Invite> invites = guild.retrieveInvites().complete();
            for (int i = 0; i < invites.size(); i++) {
                System.out.println(invites.get(i).getCode() + " - " + specialInviteCode);
                if (invites.get(i).getCode().equals(specialInviteCode)) {
                    if (inviteCount < invites.get(i).getUses()) {
                        Role role = this.guildDatabase.getSpecialRole(member.getGuild());
                        try {
                            if (role != null) {
                                member.getGuild().addRoleToMember(member.getIdLong(), role).queue();
                                System.out.println(role.getName() + " was given to " + member.getId());
                            } else {
                                System.out.println("memberid = " + (member.getGuild().getIdLong()));
                            }
                        } catch (HierarchyException e) {
                            TextChannel auditChannel = this.channelDatabase.getAuditChannel(member.getGuild());
                            //auditChannel.sendMessage("member.getAsMention()");
                            if (auditChannel == null) return;
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setTimestamp(OffsetDateTime.now());
                            builder.setColor(0xff0000); // FFF_grün
                            builder.setThumbnail(member.getGuild().getSelfMember().getUser().getAvatarUrl() == null ? member.getGuild().getSelfMember().getUser().getDefaultAvatarUrl() : member.getGuild().getSelfMember().getUser().getAvatarUrl()); // wenn AvatarUrl = null ist wird der DefaultAvatar vewendet
                            builder.addField("Name Nutzer*in / ID: ", member.getAsMention() + " / " + member.getId(), false);
                            builder.addField("SpecialRole / ID: ", role.getAsMention() + " / " + role.getId(), false);
                            builder.addField("Exeption: ", "Die SpecialRole konnte nicht an " + member.getAsMention() + " vergeben werden, da " + role.getAsMention() + " über der höchsten BotRolle steht.", false);
                            builder.setTitle(":no_pedestrians: HierarchyException:");

                            auditChannel.sendMessage(builder.build()).queue();
                        }
                        System.out.println(member.getUser().getName() + " used special code");
                        inviteCount = invites.get(i).getUses();
                        this.inviteDatabase.saveSpecialMember(member);
                    }
                }
            }
        } catch (InsufficientPermissionException e) {
            //System.err.println("InsufficientPermissionException: bot needs MANAGE_SERVER Permission!");
            //throws Exeption but executes try path afterward????
        }
    }

    public Long getGuildIDofInviteManager() { // guildID ist extern erreichbar
        return guildID;
    }

    public void setSpecialInviteCode(String newCode) {

        specialInviteCode = newCode;
        saveInviteCount();

    }
}
