package main.java.files;

public class SQLManager {
	
	public static void onCreate() {
		
		// id guildID channelID messageID emote roleID
		
		LiteSQL.onUpdate("CREATE TABLE IF NOT EXISTS reactroles(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, guildid INTEGER, channelid INTEGER, messageid INTEGER, emote VARCHAR, roleid INTEGER)");
		
	}

}
