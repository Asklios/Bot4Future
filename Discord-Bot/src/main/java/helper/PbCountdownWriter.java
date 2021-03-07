package main.java.helper;

import main.java.DiscordBot;
import main.java.files.impl.TimedTasksDatabaseSQLite;
import main.java.files.interfaces.TimedTasksDatabase;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PbCountdownWriter {

    private final TimedTasksDatabase timedTasksDatabase = new TimedTasksDatabaseSQLite();

    public static void countdown(int days, String color) {
        new PbCountdownWriter().newCountdown(days,color);
    }

    public static void countdownUpdater() {
        new PbCountdownWriter().updater();
    }

    private void newCountdown(int days, String color) {
        String type = "countdown";
        timedTasksDatabase.removeAllEntriesByType(type);
        long endTime = System.currentTimeMillis();
        int saveDays = days + 1;
        String note = saveDays + "⫠" + color;
        timedTasksDatabase.addTask(endTime, type, note);
    }

    private void updater() {
        String type = "countdown";
        String[] note = timedTasksDatabase.getFirstNoteByType(type).split("⫠");
        int lastDay = Integer.parseInt(note[0]);
        String color;
        try {
            color = note[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            color = "white";
        }

        int day = lastDay - 1;

        writeCountdown(day + "", color);

        if (day != 0) {
            long endTime = System.currentTimeMillis() + TimeMillis.millisToMidnight();

            String newNote = day + "⫠" + color;
            timedTasksDatabase.removeAllEntriesByType(type);
            timedTasksDatabase.addTask(endTime, type, newNote);
        }
        else {
            timedTasksDatabase.removeAllEntriesByType(type);
        }
    }

    private void writeCountdown(String day, String colorString) {

        SelfUser selfUser = DiscordBot.INSTANCE.jda.getSelfUser();

        Color color;

        color = new ColorFromString().getColor(colorString);


        File botPb = new File(DiscordBot.INSTANCE.getBotPbPath());
        File newPb = new File(DiscordBot.INSTANCE.getPbPath());

        if (!botPb.exists()) {
            if (!downloadPb(selfUser, botPb)) return;
        }

        if (!newPb.exists()){
            try {
                newPb.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(botPb);
            Graphics graphics = bufferedImage.getGraphics();

            if (!day.equals("0")) {
                drawString(graphics, day + "", color);
            }
            else {
                drawString(graphics,  "", color);
            }

            ImageIO.write(bufferedImage, "png", newPb);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            selfUser.getManager().setAvatar(net.dv8tion.jda.api.entities.Icon.from(newPb)).queue(c -> newPb.delete());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawString(Graphics g, String text, Color color) {

        Rectangle rect = new Rectangle();
        rect.setBounds(32, 32, 64, 64);

        Font font = new Font("Arial Black", Font.BOLD, 60);
        FontMetrics metrics = g.getFontMetrics(font);

        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();

        g.setColor(color);
        g.setFont(font);

        g.drawString(text, x, y);
    }

    private boolean downloadPb(User selfUser, File botPb) {
        try {
            botPb.createNewFile();
            String pbUrl = selfUser.getEffectiveAvatarUrl();
            URL url = null;
            try {
                url = new URL(pbUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (url == null) {
                return false;
            }
            BufferedImage c = null;
            try {
                c = ImageIO.read(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ImageIO.write(c, "png", botPb);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
