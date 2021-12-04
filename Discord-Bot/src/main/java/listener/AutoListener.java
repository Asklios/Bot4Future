package main.java.listener;

import main.java.DiscordBot;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class AutoListener {

    public ConcurrentHashMap<String, String> autoResponse;
    public ConcurrentHashMap<Long, String> ntdHashMap;

    public AutoListener() {

        autoResponse = new ConcurrentHashMap<>();
        ntdHashMap = new ConcurrentHashMap<>();
        readFile();
    }

    public void autoListen(Member member, GuildMessageChannel channel, Message message) {

        if (member.getUser().equals(member.getJDA().getSelfUser())) {
            return;
        }
        String s = message.getContentRaw().replaceAll(" ", "\\$").replaceAll("\\?", "").replaceAll("\\.", "").replaceAll("\\!", "").toLowerCase();
        if (s.length() > 3 && s.substring(0, 3).equals("ntd")) {
            performNtd(s, channel);
        }
        s = this.autoResponse.get(s); // s = festgelegte Antwort
        if (s != null) {

            channel.sendMessage(s).queue();
        }
    }

    private void readFile() {
        try {
            File file = new File(DiscordBot.INSTANCE.getAutoListenerFilePath());
            Scanner scanner = new Scanner(file, "UTF-8");

            while (scanner.hasNextLine()) {

                String line = "";
                line = scanner.nextLine();

                String[] split = line.split("\\$");
                split[0] = split[0].replaceAll(" ", "\\$").replaceAll("\\?", "").replaceAll("\\.", "").replaceAll("\\!", "").toLowerCase();
                if (split.length == 2) {

                    if (split[0].substring(0, 3).equals("ntd")) {
                        if (ntdToHashMap(split[0], split[1])) {
                            autoResponse.put(split[0], split[1]);
                        }
                    } else {
                        autoResponse.put(split[0], split[1]);
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            String path = DiscordBot.INSTANCE.getAutoListenerFilePath();
            System.err.println("Could not find autoResponseFile at " + path);
            File newFile = new File(path);

            try {
                OutputStreamWriter myWriter = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
                myWriter.write("Hier Sprüche eintragen\r\n" + "userinput$botoutput");
                myWriter.close();
                System.out.println("New file autoResponseFile created. Filename: " + newFile.getName() + " Path: " + path);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private boolean ntdToHashMap(String s1, String s2) {
        s1 = s1.replace("ntd", "");
        long index = 0;
        try {
            index = Long.parseLong(s1);
        } catch (NumberFormatException e) {
            return true;
        }

        ntdHashMap.put(index, s2);
        return false;
    }

    private void performNtd(String s, GuildMessageChannel channel) {
        s = s.replace("ntd", "");
        long index = 0;
        if (isNumeric(s)) {
            try {
                index = Long.parseLong(s);
            } catch (NumberFormatException e) {
                channel.sendMessage("Die Not-To-Do-Liste hat bisher nur " + Long.MAX_VALUE + " Einträge!").queue();
                return;
            }
        } else {
            return;
        }
        if (ntdHashMap.containsKey(index)) {

            channel.sendMessage(ntdHashMap.get(index)).queue();
        } else {
            Random rand = new Random();
            rand.setSeed(index);
            int r = rand.nextInt(ntdHashMap.size());
            try {
                channel.sendMessage(ntdHashMap.get((long) r)).queue();
            } catch (IllegalArgumentException e) {
                channel.sendMessage("Hatschii.... puh, was sagtest du?").queue();
            }
        }
    }

    private boolean isNumeric(final String s) {
        if (s == null || s.length() == 0) {
            return false;
        }

        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}

