package main.java;

import main.java.files.LiteSQL;
import main.java.files.PropertiesReader;
import main.java.files.SQLManager;
import main.java.files.impl.*;
import main.java.files.interfaces.*;
import main.java.listener.*;
import main.java.temp.UnMuteBanCheck;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.CompletionException;

public class DiscordBot {

    public static DiscordBot INSTANCE;

    public ShardManager shardMan;
    private final CommandManager cmdMan;
    private Thread gameLoop;
    private final AutoListener autoListener;
    private String autoListenerFilePath;
    private String dataFilePath;
    private String logFilePath;
    private String diagramFilePath;
    private String pbFilterPath;
    private String pbPath;
    private String newPbPath;
    private String dbFilePath;
    private String botToken;
    private List<GuildData> guildsData = new ArrayList<GuildData>();
    private String[] defIds;
    private Timer unMuteBanCheckTimer;
    private long muteBanTimerPeriod = 5 * 60 * 1000;
	private VoteDatabase voteDatabase;
    private GuildDatabase guildDatabase;
    private ChannelDatabase channelDatabase;
    private UserRecordsDatabase userRecordsDatabase;
    private GetMemberFromMessage getMemberFromMessage;
    private InviteDatabase inviteDatabase;

    long startUpTime = System.currentTimeMillis();

    public static void main(String[] args) {
        try {
            new DiscordBot();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    public DiscordBot() throws LoginException, IllegalArgumentException {
        INSTANCE = this;

        try {
            PropertiesReader props = new PropertiesReader();
            botToken = props.getBotToken();
            dataFilePath = props.getDataFilePath();
            autoListenerFilePath = props.getAutoResponsePath();
            dbFilePath = props.getDbFilePath();
            defIds = props.getDefIds();
            logFilePath = props.getLogFilePath();
            diagramFilePath = props.getDiagramFilePath();
            pbFilterPath = props.getPbFilterPath();
            pbPath = props.getPbPath();
            newPbPath = props.getNewPbPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LiteSQL.connect();
        SQLManager.onCreate();


        @SuppressWarnings("deprecation")
        DefaultShardManagerBuilder builder = new DefaultShardManagerBuilder();

        //DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(botToken);

        builder.setToken(botToken);

        // Bot activity
        builder.setActivity(Activity.playing("hello world")); // Anzeige was "spielt" der Bot
        builder.setStatus(OnlineStatus.ONLINE); // Anzeige Bot Erreichbarkeit

        this.cmdMan = new CommandManager();
        this.autoListener = new AutoListener();
        this.getMemberFromMessage = new GetMemberFromMessageFind();
        this.voteDatabase = new VoteDatabaseSQLite();
        this.guildDatabase = new GuildDatabaseSQLite();
        this.channelDatabase = new ChannelDatabaseSQLite();
        this.userRecordsDatabase = new UserRecordsDatabaseSQLite();
        this.inviteDatabase = new InviteDatabaseSQLite();

        builder.addEventListeners(new CommandListener());
        builder.addEventListeners(new AuditListener());
        builder.addEventListeners(new EventAuditListener());
        builder.addEventListeners(new ReactionListener(voteDatabase));

        try {
            shardMan = builder.build();
        } catch (LoginException | CompletionException e) {
            System.err.println("Could not start bot. Check the Bot-Token in config.properties");
            return;
        }
        System.out.println("Bot Status: online");


        
        shutdown(); //Methode shutdown wird zum überprüfen aufgerufen
        
        //Start Timer for tempMute and TempBan
        unMuteBanCheckTimer = new Timer();
        unMuteBanCheckTimer.schedule(new UnMuteBanCheck(), 1000, muteBanTimerPeriod);
        
        runLoop();

    }

    // Möglichkeit für absichtlichen Shutdown
    public void shutdown() {
        new Thread(() -> {

            String line = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while ((line = reader.readLine()) != null) {


                    if (line.equalsIgnoreCase("exit")) {


                        if (shardMan != null) {
                            shardMan.setStatus(OnlineStatus.OFFLINE);
                            shardMan.shutdown();
                            System.out.println("Bot Status: offline");
                        }

                        System.exit(0);
                    } else {
                        System.out.println("Use 'exit' to shutdown.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Cought IOException: FFF_Discord_Bot.java - shutdown");
            }

        }).start();
    }

    public boolean shutdown = false;

    //Function to shutdown through code not the exit command
    public void shutdownCode() {
        shutdown = true;
        if (shardMan != null) {
            shardMan.setStatus(OnlineStatus.OFFLINE);
            shardMan.shutdown();
            System.out.println("Bot Status: offline");
        }

        if (gameLoop != null) {
            gameLoop.interrupt();
        }
    }

    // Wechsel der Statusmedungen
    public void runLoop() {
        this.gameLoop = new Thread(() -> {

            long time = System.currentTimeMillis();

            while (!shutdown) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    System.err.println("Cought InterruptedException: FFF_Discord_Bot.java - runLoop");
                }
                if (System.currentTimeMillis() >= time + 1000) {
                    time = System.currentTimeMillis();

                    onSecond();
                }
            }

        });
        this.gameLoop.setName("Loop");
        this.gameLoop.start();
    }

    private static final String[] gameStatus = new String[]{"💚", " Server Admin.", " für %member activists.", " auf %guilds Servern!", "%help", " seit %ontime"};
    int timerNext = 15;

    public void onSecond() {
        if (timerNext <= 0) {
            Random rand = new Random(); //Status wird zufällig aus Array ausgewählt
            int i = rand.nextInt(gameStatus.length);

            shardMan.getShards().forEach(jda -> {

                long upTimeMillis = System.currentTimeMillis() - startUpTime;
                long secondsUptime = upTimeMillis / 1000;
                long minutesUptime = ((secondsUptime % 86400) % 3600) / 60;
                long hoursUptime = (secondsUptime % 86400) / 3600;
                long daysUptime = secondsUptime / 86400;

                String upTime = daysUptime + "d:" + hoursUptime + "h:" + minutesUptime + "min";

                String text = gameStatus[i].replaceAll("%guilds", "" + jda.getGuilds().size()).replaceAll("%member", "" + (jda.getUsers().size() - 1))
                        .replaceAll("%ontime", "" + (upTime));
                //System.out.println(upTime);

                jda.getPresence().setActivity(Activity.playing(text));
            });
            timerNext = 15;
        } else {
            timerNext--;
        }
    }


    //XML und.properties
    public void updateGuilds(List<Guild> guilds) {

        for (int i = 0; i < guilds.size(); i++) {
            boolean existingID = false;
            for (int j = 0; j < guildsData.size(); j++) {
                if (guildsData.get(j).getID() == guilds.get(i).getIdLong()) {
                    existingID = true;
                    break;
                }
            }
            if (!existingID) {
                GuildData newGuild = new GuildData(guilds.get(i).getIdLong());
                newGuild.setSpecialInviteCode(guildDatabase.getSpecialCode(guilds.get(i)));
                guildsData.add(newGuild);
            }
            this.guildDatabase.startUpEntries(guilds.get(i));
            this.channelDatabase.startUpEntries(guilds.get(i));
        }
    }

    public CommandManager getCmdMan() {
        return cmdMan;
    }

    public AutoListener getAutoListener() {
        return autoListener;
    }

    public String getAutoListenerFilePath() {
        return autoListenerFilePath;
    }

    public String getDataFilePath() {
        return dataFilePath;
    }

    public String getDbFilePath() {
        return dbFilePath;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public String getDiagramFilePath() {return diagramFilePath;}

    public String getPbFilterPath() {return pbFilterPath;}
    public String getPbPath() {return pbPath;}
    public String getNewPbPath() {return newPbPath;}

    public List<GuildData> getGuildsData() {
        return guildsData;
    }

    public void setSpecialRoleID(long guildID, long roleID) {

        for (int i = 0; i < guildsData.size(); i++) {

            if (guildsData.get(i).getID() == guildID) {
                guildsData.get(i).setSpecialRoleID(roleID);
            }
        }
    }

    public long getSpecialRoleID(long guildID) {
        for (int i = 0; i < guildsData.size(); i++) {
            if (guildsData.get(i).getID() == guildID) {
                return guildsData.get(i).getSpecialRoleID();
            }
        }
        return 0;
    }

    public String[] getDefIds() {
        return defIds;
    }

    public long getMuteTimerPeriod() {
		return muteBanTimerPeriod;
	}

}

// Umlautkorrektur: üben, ärgern, öffentlich, groß
