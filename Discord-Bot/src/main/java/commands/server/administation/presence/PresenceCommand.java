package main.java.commands.server.administation.presence;

import main.java.commands.server.ServerCommand;
import main.java.files.impl.CallDatabaseSQLite;
import main.java.files.interfaces.CallDatabase;
import main.java.helper.TimeMillis;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PresenceCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        CallDatabase callDatabase = new CallDatabaseSQLite();

        if (!member.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            channel.sendMessage("Du musst Kick_Members besitzen um diesen Command zu nutzen.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        //%presence <voice channel id> <callname>
        String[] messageSplit = message.getContentDisplay().split("\\s+");

        if ((messageSplit.length < 2)) {
            channel.sendMessage("Es wurde kein Voice-Channel angegeben. `%presence <voice channel id> <callname>`").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        String callName = "";

        for (int i = messageSplit.length - 1; i > 1; i--) {
            callName = String.join(messageSplit[i], " ", callName);
        }

        callName = callName.replaceFirst(" ", "");

        try {
            Long.parseLong(callName);
            channel.sendMessage("Die Bezeichnung \"" + callName + "\" kann nicht verwendet werden, da sie mit einer ID verwechselt werden könnte.")
                    .queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        } catch (NumberFormatException e) {
            //
        }

        //start time
        final long startTime = System.currentTimeMillis();

        VoiceChannel voiceChannel = channel.getGuild().getVoiceChannelById(messageSplit[1]);
        List<Member> voiceMembers = voiceChannel.getMembers();

        LinkedHashSet<Member> memberList = new LinkedHashSet<>(voiceMembers);

        if (memberList.isEmpty()) {
            channel.sendMessage("Der Voice-Channel " + voiceChannel.getName() + " ist aktuell leer.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }
        else if (callDatabase.checkName(channel.getIdLong(), callName)) {
            channel.sendMessage("Die Bezeichnung \"" + callName + "\" wurde bereits verwendet.").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        final long infoMessageId = channel.sendMessage("Anwesenheit wird in " + voiceChannel.getName() + " erfasst bis der Channel nicht mehr genutzt wird.")
                .complete().getIdLong();
        final boolean[] active = {true};
        String finalCallName = callName;

        Thread updateMembers = new Thread(() -> {
            while (active[0]) {
                memberList.addAll(voiceChannel.getMembers());
                try {
                    Thread.sleep(5*1000);
                } catch (InterruptedException e) {
                    //
                }
                //System.out.println("loop");
                if (voiceChannel.getMembers().isEmpty()) {active[0] = false;}
                channel.retrieveMessageById(infoMessageId).complete().editMessage("**Anwesende:** " + memberList.stream()
                        .map(s -> new StringBuffer(s.getEffectiveName())).collect(Collectors.joining("; "))).complete();
            }

            String memberString = memberList.stream().map(s -> new StringBuffer(s.getId())).collect(Collectors.joining("⫠"));
            long endTime = System.currentTimeMillis();

            channel.retrieveMessageById(infoMessageId).complete().editMessage("Database request...")
                    .complete();

            long callId = callDatabase.saveCallData(channel.getGuild(), memberString, startTime, endTime, finalCallName, message.getAuthor().getIdLong());

            channel.retrieveMessageById(infoMessageId).complete().editMessage("Die TK \"" + finalCallName + "\" (ID: " + callId + ") wurde nach " +
                    TimeMillis.upTime(endTime - startTime) + " beendet, die Anwesenheitsliste wurde in der Datenbank gespeichert.")
                    .complete();
        });

        updateMembers.start();
    }
}
