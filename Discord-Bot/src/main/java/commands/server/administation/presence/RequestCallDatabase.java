package main.java.commands.server.administation.presence;

import main.java.DiscordBot;
import main.java.commands.server.ServerCommand;
import main.java.files.impl.CallDatabaseSQLite;
import main.java.files.interfaces.CallDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RequestCallDatabase implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {

        CallDatabase callDatabase = new CallDatabaseSQLite();

        //%getpresence <id oder name>
        String[] messageSplit = message.getContentDisplay().split("\\s+");

        String search = "";

        for (int i = 1; i < messageSplit.length; i++) {
            search += " " + messageSplit[i];
        }
        search = search.replaceFirst(" ", "");

        if (search.equals("")) {
            channel.sendMessage("Es wurde kein Suchbegriff eingegeben. `%getpresence <id oder name>`").queue();
            return;
        }

        String users = callDatabase.getUsersFromDb(search, channel, member);

        if (users == null) return;
        if (users.isEmpty()) return;

        String[] userIds = users.split("⫠");
        Guild guild = channel.getGuild();

        ArrayList<Member> members = new ArrayList<>();

        String finalSearch = search;
        guild.retrieveMembersByIds(userIds).onSuccess(m -> {
            members.addAll(m);

            String path = DiscordBot.INSTANCE.getLogFilePath();
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            File file = new File(DiscordBot.INSTANCE.getLogFilePath());
            String fileName = "presence " + OffsetDateTime.now().format(dateFormat) + ".txt";
            EmbedBuilder embed = new EmbedBuilder();

            List<String> textList = new ArrayList<>();
            textList.add("Anwesenheitsliste der TK \"" + finalSearch + "\"");

            for (Member mem : members) {
                try {
                    String name = mem.getEffectiveName();
                    String id = mem.getId();
                    textList.add(id + ", " + name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                OutputStreamWriter myWriter = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);

                String text = String.join("\r\n", textList);

                myWriter.write(text);
                myWriter.close();

            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }

            embed.setTitle("Anwesenheitsliste \"" + finalSearch + "\" ");

            try {
                member.getUser().openPrivateChannel().queue(p -> p.sendFile(file, fileName).embed(embed.build()).queue());
            } catch (ErrorResponseException e) {
                channel.sendMessage(member.getAsMention() + ", der Bot kann dir keine PN schicken. Bitte überprüfe deine Privatsphäreeinstellungen.")
                        .queue(me -> me.delete().queueAfter(5,TimeUnit.SECONDS));
            }
        });
    }
}
