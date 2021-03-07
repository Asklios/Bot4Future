package main.java.files;

import main.java.DiscordBot;
import main.java.commands.server.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class DiagramGenerator implements ServerCommand {

    private int width = 500;
    private int height = 80;

    @Override
    public void performCommand(Member member, TextChannel channel, Message message){


        Integer[] values = {5,5,5,5,10,10,10,10,10,5,5,5,5,5,5};
        Arrays.sort(values, Comparator.reverseOrder());


        try {
            generateDiagram(values);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Prozentwerte in values
    private void generateDiagram(Integer[] values) throws IOException {

        File path = new File(DiscordBot.INSTANCE.getDiagramFilePath());

        BufferedImage img = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = img.createGraphics();

        float a = 0;

        for (int i = 0; i < values.length; i++) {
            float k = width * values[i] / 100;

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
        ImageIO.write(img, "PNG", new File(path, ""));
    }
}
