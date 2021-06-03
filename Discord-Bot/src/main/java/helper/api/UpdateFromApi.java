package main.java.helper.api;

import main.java.DiscordBot;
import main.java.helper.TimeMillis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class UpdateFromApi {

    public void completeUpdate() {
        String apiUrlString = "https://api.fffutu.re/v1/";

        String localGroups = null;
        String strikes = null;

        try {
            List<String> localGroupsLines = readUrl(apiUrlString + "localGroups");
            List<String> strikeLines = readUrl(apiUrlString + "strike");

            localGroups = String.join("", localGroupsLines);
            strikes = String.join("", strikeLines);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert localGroups != null;

        new LocalGroups().deleteAll();
        new Strikes().deleteAll();

        addLocalGroups(localGroups);
        addStrikes(strikes);

        DiscordBot.INSTANCE.notifier.handleNotify();
        DiscordBot.INSTANCE.subscribtionDatabase.getUpdateDatabase().cleanupDatabase();

        DiscordBot.POOL.schedule(() -> {
            new UpdateFromApi().completeUpdate();
        }, 30, TimeUnit.MINUTES);
    }

    private List<String> readUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        BufferedReader reader = null;

        while (reader == null) {
            try {
                reader = new BufferedReader(new InputStreamReader(url.openStream()));
            } catch (UnknownHostException e) {
                try {
                    Thread.sleep(1000*60*15);
                } catch (InterruptedException f) {
                    f.printStackTrace();
                }
            }
        }

        String line;
        List<String> lines = new ArrayList<>();
        while ((line = reader.readLine()) != null)
        {
            lines.add(line);
        }
        return lines;
    }

    private void addLocalGroups(String localGroups) {
        String[] localGroupsSplit = localGroups.replaceAll("\\[", "").replaceAll("]", "")
                .split("},\\{");
        for (String s : localGroupsSplit) {
            s = s.replaceAll("\\{", "").replaceAll("}", "");
            String[] infoTypeSplit = s.split(",");

            String idString = null;
            String name = null;
            String latString = null;
            String lonString = null;
            String state = null;
            String facebook = null;
            String instagram = null;
            String twitter = null;
            String youtube = null;
            String website = null;
            String whatsapp = null;
            String telegram = null;
            String other = null;
            String email = null;

            for (String i : infoTypeSplit) {
                String[] infoSplit = i.split(":");
                String key = infoSplit[0].replaceAll("\"", "");
                String value = "";
                    for (int j = 1; j < infoSplit.length; j++) {
                        if (value.equals("")) value = infoSplit[j].replaceAll("\"", "");
                        else value = value + ":" + infoSplit[j].replaceAll("\"", "");
                    }

                switch (key) {
                    case "id": idString = value; break;
                    case "name": name = value; break;
                    case "lat": latString = value; break;
                    case "lon": lonString = value; break;
                    case "state": state = value; break;
                    case "facebook": facebook = value; break;
                    case "instagram": instagram = value; break;
                    case "twitter": twitter = value; break;
                    case "youtube": youtube = value; break;
                    case "website": website = value; break;
                    case "whatsapp": whatsapp = value; break;
                    case "telegram": telegram = value; break;
                    case "other": other = value; break;
                    case "email": email = value; break;
                }
            }

            assert idString != null;
            long id = Integer.parseInt(idString);
            double lat;
            if (latString.equals("null")) lat = 0;
            else lat = Double.parseDouble(latString);
            double lon;
            if (lonString.equals("null")) lon = 0;
            else lon = Double.parseDouble(lonString);

            if (name.equals("null")) name = null;
            if (state.equals("null")) state = null;
            if (facebook.equals("null")) facebook = null;
            if (instagram.equals("null")) instagram = null;
            if (twitter.equals("null")) twitter = null;
            if (youtube.equals("null")) youtube = null;
            if (website.equals("null")) website = null;
            if (whatsapp.equals("null")) whatsapp = null;
            if (telegram.equals("null")) telegram = null;
            if (other.equals("null")) other = null;
            if (email.equals("null")) email = null;

            new LocalGroups().addLocalGroup(id, name, lat, lon, state, facebook, instagram, twitter, youtube, website, whatsapp,
                    telegram, other, email);
        }
    }

    private void addStrikes(String strikes) {
        String[] StrikesSplit = strikes.replaceAll("\\[", "").replaceAll("]", "")
                .split("},\\{");
        for (String s : StrikesSplit) {
            s = s.replaceAll("\\{", "").replaceAll("}", "");
            String[] infoTypeSplit = s.split(",");

            String idString = null;
            String locationName = null;
            String localGroupName = null;
            String latString = null;
            String lonString = null;
            String state = null;
            String dateTimeString = null;
            String note = null;
            String eventLink = null;
            String lastUpdateString = null;

            for (String i : infoTypeSplit) {
                String[] infoSplit = i.split(":");
                String key = infoSplit[0].replaceAll("\"", "");
                String value = "";
                for (int j = 1; j < infoSplit.length; j++) {
                    if (value.equals("")) value = infoSplit[j].replaceAll("\"", "");
                    else value = value + ":" + infoSplit[j].replaceAll("\"", "");
                }

                switch (key) {
                    case "id": idString = value; break;
                    case "locationName": locationName = value; break;
                    case "localGroupName": localGroupName = value; break;
                    case "lat": latString = value; break;
                    case "lon": lonString = value; break;
                    case "state": state = value; break;
                    case "dateTime": dateTimeString = value.replaceAll("T", ""); break;
                    case "note": note = value; break;
                    case "eventLink": eventLink = value; break;
                    case "lastUpdate": lastUpdateString = value.replaceAll("T", ""); break;
                }
            }

            try {
                if (idString.equals("null")) idString = null;
                if (locationName.equals("null")) locationName = null;
                if (localGroupName.equals("null")) localGroupName = null;
                if (latString.equals("null")) latString = null;
                if (lonString.equals("null")) lonString = null;
                if (state.equals("null")) state = null;
                if (dateTimeString.equals("null")) dateTimeString = null;
                if (note.equals("null")) note = null;
                if (eventLink.equals("null")) eventLink = null;
                if (lastUpdateString.equals("null")) lastUpdateString = null;
            } catch (NullPointerException e) {
                System.err.println("The API might have changed! (" + this.getClass().getName() +
                        " line: " + Thread.currentThread().getStackTrace()[1].getLineNumber() + ")");
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            long id = Long.parseLong(idString);
            double lat;
            if (latString == null) lat = 0;
            else lat = Double.parseDouble(latString);
            double lon;
            if (lonString == null) lon = 0;
            else lon = Double.parseDouble(lonString);
            Date dateTime;
            if (dateTimeString == null) dateTime = null;
            else dateTime = TimeMillis.stringToDate(dateTimeString);
            Date lastUpdate;
            if (lastUpdateString == null) lastUpdate = null;
            else lastUpdate = TimeMillis.stringToDate(lastUpdateString);


            new Strikes().addStrike(id, locationName, localGroupName, lat, lon, state, dateTime, note, eventLink, lastUpdate);
        }
    }
}
