package main.java.commands.server.administation.poll;

import main.java.commands.server.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class GetPollCommand implements ServerCommand {

    private int width = 500;
    private int height = 80;

    @Override
    public void performCommand(Member member, TextChannel channel, Message message) {
/*
        if (!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            return;
        }
        // %getpoll #channel <MessageId>
        Guild guild = channel.getGuild();
        long guildID = guild.getIdLong();
        long channelID = channel.getIdLong();
        String[] messageSplit = message.getContentDisplay().split("\\s+");
        long textChannelId = message.getMentionedChannels().get(0).getIdLong();
        String messageIdString = messageSplit[2];
        long messageID = 0;

        try {
            guild.getTextChannelById(textChannelId).retrieveMessageById(messageIdString).complete();
        } catch (IllegalArgumentException | InsufficientPermissionException e) {
            channel.sendMessage("Die Nachricht konnte nicht gefunden werden oder der Bot hat keine Berechtigung sie zu lesen").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }
        messageID = Long.parseLong(messageIdString);

        ResultSet set = LiteSQL.onQuery("SELECT emotes, texts, value FROM votereactions WHERE guildid = " +
                        guildID + " AND channelid = " + channelID + " AND messageid = " + messageID);

        String[] emoteString;
        String[] textString;
        String[] valueString;

        try {
            emoteString = set.getString("emotes").replaceAll(" ","").split("⫠");
            textString = set.getString("texts").split("⫠");
            valueString = set.getString("value").split("⫠");
        } catch (SQLException e) {
            //gelöscht
            channel.sendMessage("Diese Abstimmung existiert nicht(mehr).").queue(m -> m.delete().queueAfter(5,TimeUnit.SECONDS));
            return;
        }

        EmbedBuilder b = new EmbedBuilder();
        b.setColor(0x1da64a); //FFF grün

        for (int i = 0; i < emoteString.length; i++){
            b.appendDescription(valueString[i] + "x " + emoteString[i] + " - " + textString[i] + "\n \n");
        }

        //Diagram generator
        Integer[] valuesInt = Arrays.stream(valueString).map(Integer::parseInt).toArray(Integer[]::new);
        int sum = Arrays.stream(valuesInt).reduce(0, Integer::sum);
        if (sum == 0) {
            //wenn Summe der abgegebenen Stimmen = 0
            channel.sendMessage(b.build()).queue();
            return;
        }
        Float[] values = Arrays.stream(valuesInt).map(integer -> integer/(float)sum).toArray(Float[]::new);

        try {
            generateDiagram(values);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File diagram = new File(DiscordBot.INSTANCE.getDiagramFilePath());

        channel.sendMessage(b.build()).addFile(diagram).queue();
    }

    private void generateDiagram(Float[] values) throws IOException {

        File path = new File(DiscordBot.INSTANCE.getDiagramFilePath());

        BufferedImage img = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = img.createGraphics();

        float a = 0;

        for (int i = 0; i < values.length; i++) {
            float k = (width * values[i]);

            String[] colors = {"0xf95d6a", "0xa05195", "0x003f5c", "0xd45087", "0x2f4b7c", "0xff7c43", "0x665191", "0xffa600"};

            int c = 0;
            int valuelength = values.length;
            int value = 0;

            for (int v = valuelength; v >= 0; v--) {
                if (c >= colors.length) {c = 0;}
                if (i == value) {
                    g2d.setColor(Color.decode(colors[c]));
                }
                c++;
                value++;
            }

            g2d.setBackground(Color.decode("0x000000"));

            for (int x = 0; x < k; x++) {
                // n += 50;
                g2d.drawLine((int)a + x,0, (int) a + x, height);
            }
            a += k;
        }
        ImageIO.write(img, "PNG", new File(path, ""));*/
    }
}
