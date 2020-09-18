package main.java.commands.user;

import main.java.DiscordBot;
import main.java.commands.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ProfilePictureGeneratorCommand implements ServerCommand {

    @Override
    public void performCommand(Member member, TextChannel channel, Message message){

        String filterPicPath = DiscordBot.INSTANCE.getPbFilterPath();
        String pbPath = DiscordBot.INSTANCE.getPbPath();
        String newPbPath = DiscordBot.INSTANCE.getNewPbPath();

        String profilePicUrl = (member.getUser().getAvatarUrl() == null ? member.getUser().getDefaultAvatarUrl() : member.getUser().getAvatarUrl());

        URL url = null;
        try {
            url = new URL(profilePicUrl);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url == null) {return;}
        BufferedImage c = null;
        try {
            c = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }


        ImageIcon pb = new ImageIcon(c);
        Image profilePic = new BufferedImage(128, 128,
                BufferedImage.TYPE_INT_ARGB);

        pb.paintIcon(new JCheckBox(), profilePic.getGraphics(), 0, 0);
        Image filterPic = new ImageIcon(filterPicPath).getImage();

        int w = 128;
        int h = 128;
        BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics g = combined.getGraphics();

        g.drawImage(profilePic, 0, 0, null);
        g.drawImage(filterPic, 0, 0, null);

        g.dispose();

        try {
            ImageIO.write(combined, "PNG", new File(newPbPath, ""));
        } catch (IOException e) {
            e.printStackTrace();
        }

        File newPb = new File(newPbPath);

        channel.sendFile(newPb).complete();

        newPb.delete();
        File pbpng = new File(pbPath);
        pbpng.delete();
    }
}
