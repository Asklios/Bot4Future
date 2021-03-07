package main.java.commands.privateMessage.pb;

import main.java.DiscordBot;
import main.java.commands.privateMessage.PrivateCommand;
import main.java.helper.PbCountdownWriter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.Arrays;

public class PbCountdownPmCommand implements PrivateCommand {

    @Override
    public void performCommand(User user, PrivateChannel channel, Message message) {

        if (!Arrays.asList(DiscordBot.INSTANCE.getDefIds()).contains(user.getId())) {
            channel.sendMessage("Dieser Command ist nur für die Botentwickler*innen vorgesehen.").queue();
            return;
        }

        //%pbcountdown 5 green
        String[] messageSplit = message.getContentDisplay().split("\\s+");
        int days;
        String color = null;

        try {
            days = Integer.parseInt(messageSplit[1]);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            channel.sendMessage("```%pbcountdown <intDays> <color(green, blue, ...)>```").queue();
            return;
        }
        catch (NumberFormatException e) {
            channel.sendMessage("```%pbcountdown <intDays> <color(green, blue, ...)>``` Die Dauer muss als int angegeben werden.").queue();
            return;
        }

        try {
            color = messageSplit[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            //
        }

        PbCountdownWriter.countdown(days, color);
    }
}
