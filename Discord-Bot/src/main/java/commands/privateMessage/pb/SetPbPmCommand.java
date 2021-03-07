package main.java.commands.privateMessage.pb;

import main.java.DiscordBot;
import main.java.commands.privateMessage.PrivateCommand;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class SetPbPmCommand implements PrivateCommand {

    @Override
    public void performCommand(User user, PrivateChannel channel, Message message) {

        if (!Arrays.asList(DiscordBot.INSTANCE.getDefIds()).contains(user.getId())) {
            channel.sendMessage("Dieser Command ist nur für die Botentwickler*innen vorgesehen.").queue();
            return;
        }

        if (message.getAttachments().isEmpty()) {
            channel.sendMessage("Du musst eine .png Datei anhängen.").queue();
            return;
        }

        Message.Attachment attachment = message.getAttachments().get(0);
        if (attachment.getFileExtension() == null || !attachment.getFileExtension().equals("png")) {
            channel.sendMessage("Es ist eine .png Datei erforderlich.").queue();
            return;
        }

        channel.sendMessage("Hier ist das alte Profilbild. **Achtung:** es ist nur noch eine begrenzte Zeit lang abrufbar!"
                + channel.getJDA().getSelfUser().getEffectiveAvatarUrl()).queue();

        Icon pbIcon = null;
        try {
            pbIcon = attachment.retrieveAsIcon().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        channel.getJDA().getSelfUser().getManager().setAvatar(pbIcon).queue();

        File pb = new File(DiscordBot.INSTANCE.getBotPbPath());
        if (!pb.exists()) {
            try {
                pb.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        attachment.downloadToFile(pb);
    }
}
