package main.java;

import main.java.activitylog.ActivitySQLManager;
import main.java.activitylog.EventAudit;
import main.java.activitylog.LiteSQLActivity;
import main.java.commands.privateMessage.PrivateCommandManager;
import main.java.commands.server.CommandManager;
import main.java.commands.slash.SlashCommandManager;
import main.java.files.LiteSQL;
import main.java.files.LiteSqlClear;
import main.java.files.PropertiesReader;
import main.java.files.SQLManager;
import main.java.files.impl.*;
import main.java.files.interfaces.*;
import main.java.helper.GetMemberFromMessage;
import main.java.helper.TimedTasks;
import main.java.helper.api.UpdateFromApi;
import main.java.listener.*;
import main.java.util.PollManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DiscordBot {

    public static DiscordBot INSTANCE;
    public boolean shutdown = false;
    public JDA jda;
    private final CommandManager cmdMan;
    private final PrivateCommandManager privCmdMan;
    private Thread gameLoop;
    private final AutoListener autoListener;
    private String autoListenerFilePath;
    private String logFilePath;
    private String diagramFilePath;
    private String botPbPath;
    private String pbFilterPath;
    private String pbPath;
    private String newPbPath;
    private String dbFilePath;
    private String botToken;
    private List<GuildData> guildsData = new ArrayList<>();
    private String[] defIds;
    private long muteBanTimerPeriod = 5 * 60 * 1000;
	private VoteDatabase voteDatabase;
    private RoleDatabase roleDatabase;
    private ChannelDatabase channelDatabase;
    private UserRecordsDatabase userRecordsDatabase;
    private GetMemberFromMessage getMemberFromMessage;
    private InviteDatabase inviteDatabase;
    private TimedTasksDatabase timedTasksDatabase;
    private SelfRoles selfRoles;

    public PollManager pollManager;

    public SlashCommandManager slashCommandManager = new SlashCommandManager();

    public static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(5);
    public static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("d.MM.yy, k:mm");


    long startUpTime = System.currentTimeMillis();

    public static void main(String[] args) throws SQLException {
        try {
            new DiscordBot(args);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    public DiscordBot(String[] args) throws IllegalArgumentException, SQLException {
        INSTANCE = this;

        try {
            PropertiesReader props = new PropertiesReader();
            botToken = props.getBotToken();
            autoListenerFilePath = props.getAutoResponsePath();
            dbFilePath = props.getDbFilePath();
            defIds = props.getDefIds();
            logFilePath = props.getLogFilePath();
            diagramFilePath = props.getDiagramFilePath();
            botPbPath = props.getBotPbFilePath();
            pbFilterPath = props.getPbFilterPath();
            pbPath = props.getPbPath();
            newPbPath = props.getNewPbPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LiteSQL.connect();
        SQLManager.onCreate();

        LiteSQLActivity.connect();
        ActivitySQLManager.onCreate();
        pollManager = new PollManager();
        pollManager.database.loadAllPolls();
        new EventAudit().updateIgnoredChannels();

        JDABuilder builder = JDABuilder.createDefault(botToken);
        builder.enableIntents(GatewayIntent.GUILD_PRESENCES);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.enableCache(CacheFlag.MEMBER_OVERRIDES);

        builder.setToken(botToken);

        // Bot activity
        builder.setActivity(Activity.playing("hello world"));
        builder.setStatus(OnlineStatus.ONLINE);

        this.cmdMan = new CommandManager();
        this.privCmdMan = new PrivateCommandManager();
        this.autoListener = new AutoListener();
        this.getMemberFromMessage = new GetMemberFromMessage();
        this.voteDatabase = new VoteDatabaseSQLite();
        this.roleDatabase = new RoleDatabaseSQLite();
        this.channelDatabase = new ChannelDatabaseSQLite();
        this.userRecordsDatabase = new UserRecordsDatabaseSQLite();
        this.inviteDatabase = new InviteDatabaseSQLite();
        this.timedTasksDatabase = new TimedTasksDatabaseSQLite();
        this.selfRoles = new SelfRolesSQLite();

        builder.addEventListeners(new CommandListener());
        builder.addEventListeners(new EventAuditListener());
        builder.addEventListeners(new AuditListener());
        builder.addEventListeners(new ReactionListener(voteDatabase));
        builder.addEventListeners(new BumpListener());
        builder.addEventListeners(new CookieListener());
        builder.addEventListeners(new QuestionListener());

        try {
            jda = builder.build();
        } catch (LoginException | CompletionException e) {
            System.err.println("Could not start bot. Check the Bot-Token in config.properties");
            return;
        }
        System.out.println("Bot Status: online");

        slashCommandManager.startupGuilds();

        shutdown();

        System.out.println("loaded userRecords");

        selfRoles.loadSelfRoles();
        selfRoles.cleanUp();

        //start timedTasks - depends on UserRecords
        TimedTasks.startTimedTasks();
        timedTasksDatabase.updateAllTasksFromDb();
        timedTasksDatabase.removeAllEntriesByType("APIUPDATE");
        timedTasksDatabase.removeAllEntriesByType("DBCLEAR");

        LiteSqlClear.timedClearDatabase();
        System.out.println("started TimedTasks");

        //schedules api updates - depends on TimedTasks
        new UpdateFromApi().completeUpdate();
        System.out.println("API integrated");

        runLoop();
    }

    // Möglichkeit für absichtlichen Shutdown
    public void shutdown() {
        new Thread(() -> {

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while ((line = reader.readLine()) != null) {

                    switch (line) {
                        case "exit":
                            jda.shutdown();
                            System.exit(0);
                            break;
                        case "deletepolls":
                            jda.shutdown();
                            try {

                                LiteSQL.onUpdate("DROP TABLE polls;");
                                LiteSQL.onUpdate("DROP TABLE pollvotes;");
                                LiteSQL.onUpdate("DROP TABLE pollchoices;");
                                System.out.println("Umfragendatenbank vollständig gelöscht.");
                                System.out.println("Stoppe Bot...");
                            } finally {
                                System.exit(0);
                            }
                        default:
                            System.out.println("Use 'exit' to shutdown.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Caught IOException: FFF_Discord_Bot.java - shutdown");
            }
        }).start();
    }

    //Function to shutdown through code not the exit command
    public void shutdownCode() {
        pollManager.saveVotes();
        shutdown = true;
        if (jda != null) {
            jda.shutdown();
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

    private static final String[] gameStatus = new String[]{"💚", " Server Admin.",/* " für %member activists.",*/ " auf %guilds Servern!", "%help", " seit %ontime"};
    int timerNext = 15;

    public void onSecond() {
        if (timerNext <= 0) {
            Random rand = new Random(); //Status wird zufällig aus Array ausgewählt
            int i = rand.nextInt(gameStatus.length);

            long upTimeMillis = System.currentTimeMillis() - startUpTime;
            long secondsUptime = upTimeMillis / 1000;
            long minutesUptime = ((secondsUptime % 86400) % 3600) / 60;
            long hoursUptime = (secondsUptime % 86400) / 3600;
            long daysUptime = secondsUptime / 86400;

            String upTime = daysUptime + "d:" + hoursUptime + "h:" + minutesUptime + "min";

            String text = gameStatus[i].replaceAll("%guilds", "" + jda.getGuilds().size())
                    //.replaceAll("%member", "" + (jda.getUsers().size() - 1))
                    .replaceAll("%ontime", "" + (upTime));

            jda.getPresence().setActivity(Activity.playing(text));

            timerNext = 15;
        } else {
            timerNext--;
        }
    }

    //update .properties
    public void updateGuilds(List<Guild> guilds) {

        for (Guild guild : guilds) {
            boolean existingID = false;
            for (GuildData guildsDate : guildsData) {
                if (guildsDate.getID() == guild.getIdLong()) {
                    existingID = true;
                    break;
                }
            }
            if (!existingID) {
                GuildData newGuild = new GuildData(guild.getIdLong());
                newGuild.setSpecialInviteCode(roleDatabase.getSpecialCode(guild));
                guildsData.add(newGuild);
            }
            this.roleDatabase.startUpEntries(guild);
            this.channelDatabase.startUpEntries(guild);
            new EventAudit().ignoredChannelsStartUpEntries(guild);
        }
    }

    public CommandManager getCmdMan() {
        return cmdMan;
    }

    public PrivateCommandManager getPrivCmdMan() {
        return privCmdMan;
    }

    public AutoListener getAutoListener() {
        return autoListener;
    }

    public String getAutoListenerFilePath() {
        return autoListenerFilePath;
    }
    public String getDbFilePath() {
        return dbFilePath;
    }
    public String getLogFilePath() {
        return logFilePath;
    }
    public String getDiagramFilePath() {return diagramFilePath;}
    public String getBotPbPath() {return botPbPath;}
    public String getPbFilterPath() {return pbFilterPath;}
    public String getPbPath() {return pbPath;}
    public String getNewPbPath() {return newPbPath;}

    public List<GuildData> getGuildsData() {
        return guildsData;
    }

    public String[] getDefIds() {
        return defIds;
    }

    public long getMuteTimerPeriod() {
		return muteBanTimerPeriod;
	}
}
