package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletionException;

import javax.security.auth.login.LoginException;

import main.java.files.GuildDataXmlReadWrite;
import main.java.files.PropertiesReader;
import main.java.listener.AuditListener;
import main.java.listener.AutoListener;
import main.java.listener.CommandListener;
import main.java.listener.EventAuditListener;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

public class DiscordBot {
	
	public static DiscordBot INSTANCE;

	public ShardManager shardMan;
	private CommandManager cmdMan;
	private Thread gameLoop;
	private AutoListener autoListener;
	private String autoListenerFilePath;
	private String dataFilePath;
	private String logFilePath;
	private String botToken;
	private List <GuildData> guildsData = new ArrayList <GuildData>();
	private String[] defIds;
	
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
			defIds = props.getDefIds();
			logFilePath = props.getLogFilePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		@SuppressWarnings("deprecation")
		DefaultShardManagerBuilder builder = new DefaultShardManagerBuilder();
		
		//DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(botToken);
		
		builder.setToken(botToken);
		
		// Bot activity
		builder.setActivity(Activity.playing("hello world")); // Anzeige was "spielt" der Bot
		builder.setStatus(OnlineStatus.ONLINE); // Anzeige Bot Erreichbarkeit
		
		this.cmdMan = new CommandManager();
		this.autoListener = new AutoListener();
		
		builder.addEventListeners(new CommandListener());
		builder.addEventListeners(new AuditListener());
		builder.addEventListeners(new EventAuditListener());
		try {
			shardMan = builder.build();
		} catch(LoginException | CompletionException e) {
			System.err.println("Could not start bot. Check the Bot-Token in config.properties");
			return;
		}
		guildsData = GuildDataXmlReadWrite.readGuildData();
		System.out.println("Bot Status: online");
		
		shutdown(); //Methode shutdown wird zum überprüfen aufgerufen
		runLoop();  

	}
	
	// Möglichkeit für absichtlichen Shutdown
	public void shutdown() {
		new Thread(() ->  {
			
			String line = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			try {
				while((line = reader.readLine()) !=null) {
					
					
					if(line.equalsIgnoreCase("exit")) {
						shutdown = true;
						if (shardMan != null) {
							shardMan.setStatus(OnlineStatus.OFFLINE);
							shardMan.shutdown();
							System.out.println("Bot Status: offline");
						}
						
						if(gameLoop != null) {
							gameLoop.interrupt();
						}
						reader.close();
					}
					else {
						System.out.println("Use 'exit' to shutdown.");
					}	
				}
			}catch (IOException e) {
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
		
		if(gameLoop != null) {
			gameLoop.interrupt();
		}
	}
	
	// Wechsel der Statusmedungen
	public void runLoop() {
		this.gameLoop = new Thread(() -> {
			
			long time = System.currentTimeMillis();
			
			while(!shutdown) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					System.err.println("Cought InterruptedException: FFF_Discord_Bot.java - runLoop");
				}
				if(System.currentTimeMillis() >= time + 1000) {
					time = System.currentTimeMillis();
					
					onSecond();
				}
			}
			
		});
		this.gameLoop.setName("Loop");
		this.gameLoop.start();
	}
	
	String[] gameStatus = new String[] {"hello world", " Server Admin.", " für %member activists.", " auf %guilds Servern!", "%help"};
	int timerNext = 15;
	
	public void onSecond() {
		if(timerNext <= 0) {
			Random rand = new Random(); //Status wird zufällig aus Array ausgewählt
			int i = rand.nextInt(gameStatus.length);
			
			shardMan.getShards().forEach(jda -> {
				
				String text = gameStatus[i].replaceAll("%guilds", "" + jda.getGuilds().size()).replaceAll("%member", "" + (jda.getUsers().size() - 1));
				
				
				jda.getPresence().setActivity(Activity.playing(text));
			});
			timerNext = 15; 
		}
		else {
			timerNext--;
		}
	}
	
	
	
	public void updateGuilds(List <Guild> guilds) {
		
		for (int i = 0; i < guilds.size(); i++){
			boolean existingID = false;
			for (int j = 0; j < guildsData.size(); j++) {
				if (guildsData.get(j).getID() == guilds.get(i).getIdLong()) {
					existingID = true;
					break;
				}
			}
			if (!existingID) {
				GuildData newGuild = new GuildData(guilds.get(i).getIdLong());
				guildsData.add(newGuild);
				GuildDataXmlReadWrite.writeNewGuild(newGuild);
			}
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
	
	public String getLogFilePath() {
		return logFilePath;
	}
	
	public List <GuildData> getGuildsData() {
		return guildsData;
	}
	
	public void setSpecialRoleID(long guildID, long roleID) {
		
		for (int i = 0; i < guildsData.size(); i++) {
			
			if(guildsData.get(i).getID() == guildID) {
				guildsData.get(i).setSpecialRoleID(roleID);
			}
		}
	}
	
	public long getSpecialRoleID(long guildID) {
		for (int i = 0; i < guildsData.size(); i++) {
			if(guildsData.get(i).getID() == guildID) {
				return guildsData.get(i).getSpecialRoleID();
			}
		}
		return 0;
	}

	public String[] getDefIds() {
		return defIds;
	}
}

// Umlautkorrektur: üben, ärgern, öffentlich, groß
