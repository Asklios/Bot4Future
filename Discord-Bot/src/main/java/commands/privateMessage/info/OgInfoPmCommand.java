package main.java.commands.privateMessage.info;

import main.java.commands.privateMessage.PrivateCommand;
import main.java.helper.api.LocalGroup;
import main.java.helper.api.LocalGroups;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.TimeUnit;

public class OgInfoPmCommand implements PrivateCommand {

    @Override
    public void performCommand(User user, PrivateChannel channel, Message message) {
        String[] messageSplit = message.getContentDisplay().split("\\s+");

        //%og <Ortsgruppe>
        String searchString = "";

        for (int i = 1; i < messageSplit.length; i++) {
            if (searchString.equals("")) searchString = messageSplit[i];
            else searchString = searchString + " " + messageSplit[i];
        }

        LocalGroup localGroup = new LocalGroups().getGroupByName(searchString.toLowerCase());

        if (localGroup == null) {
            channel.sendMessage("Die Ortsguppe " + searchString + " konnte nicht gefunden werden.").queue(m -> m.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }
        //"https://fridaysforfuture.de/wp-content/uploads/2019/04/cropped-icon-270x270.png"
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(0x1DA64A);
        if (localGroup.getWebsite() != null) b.setAuthor("OG " + localGroup.getName() + " (" + localGroup.getState() + ")", localGroup.getWebsite(),
                "https://www.google.com/s2/favicons?sz=128&domain_url=" + localGroup.getWebsite());
        else b.setAuthor("OG " + localGroup.getName() + " (" + localGroup.getState() + ")", "https://fridaysforfuture.de/",
                "https://www.google.com/s2/favicons?sz=128&domain_url=https://fridaysforfuture.de/");
        b.setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getEffectiveAvatarUrl());
        if (localGroup.getInstagram() != null) b.setThumbnail(localGroup.getInstagram());
        if (localGroup.getFacebook() != null) b.addField("Facebook: ", localGroup.getFacebook(), false);
        if (localGroup.getInstagram() != null) b.addField("Instagram: ", localGroup.getInstagram(), false);
        if (localGroup.getTwitter() != null) b.addField("Twitter: ", localGroup.getTwitter(), false);
        if (localGroup.getYoutube() != null) b.addField("YouTube: ", localGroup.getYoutube(), false);
        if (localGroup.getWhatsapp() != null) b.addField("WhatsApp: ", localGroup.getWhatsapp(), false);
        if (localGroup.getTelegram() != null) b.addField("Telegram: ", localGroup.getTelegram(), false);
        if (localGroup.getEmail() != null) b.addField("Email: ", localGroup.getEmail(), false);
        if (localGroup.getOther() != null) b.addField("Other: ", localGroup.getOther(), false);
        channel.sendMessage(b.build()).queue(m -> m.delete().queueAfter(1, TimeUnit.MINUTES));
    }
}
