package main.java.commands.privateMessage.pb;

import main.java.DiscordBot;
import main.java.commands.privateMessage.PrivateCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class SetPbFilterCommand implements PrivateCommand {

    @Override
    public void performCommand(User user, PrivateChannel channel, Message message) {

        if (!Arrays.asList(DiscordBot.INSTANCE.getDefIds()).contains(user.getId())) {
            channel.sendMessage("Dieser Command ist nur für die Botentwickler*innen vorgesehen.").queue();
            return;
        }

        Message.Attachment attachment = null;
        try {
            attachment = message.getAttachments().get(0);
        } catch (IndexOutOfBoundsException e) {
            channel.sendMessage("Es wurde kein Anhang gefunden. Der Anhang muss eine .png Datei sein.").queue();
            return;
        }

        if (attachment.getFileExtension() == null || !attachment.getFileExtension().equals("png")) {
            channel.sendMessage("Es ist eine .png Datei erforderlich.").queue();
            return;
        }

        File pbFilter = new File(DiscordBot.INSTANCE.getPbFilterPath());
        if (!pbFilter.exists()) {
            try {
                pbFilter.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else channel.sendFile(pbFilter, "pbfilter.png").queue();

        BufferedImage[] oldPbBufferedImage = new BufferedImage[1];
        try {
            oldPbBufferedImage[0] = ImageIO.read(pbFilter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        attachment.downloadToFile(pbFilter).whenCompleteAsync((file, throwable) -> {
            File pbFilterNew = new File(DiscordBot.INSTANCE.getPbFilterPath());

            BufferedImage newPb = null;
            try {
                newPb = ImageIO.read(pbFilterNew);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert newPb != null;
            if (newPb.getWidth() != 128 || newPb.getHeight() != 128) {
                channel.sendMessage("Das Bild benotigt eine Abmessung von 128x128 px.").queue();
                try {
                    ImageIO.write(oldPbBufferedImage[0], "png", pbFilter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
