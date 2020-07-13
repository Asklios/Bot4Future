package de.java.main.files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.java.main.DiscordBot;
import de.java.main.GuildData;
import net.dv8tion.jda.api.entities.Member;

public class GuildDataXmlReadWrite {
	
	public static void writeNewGuild(GuildData newGuild) {
		File xmlFile = new File(DiscordBot.INSTANCE.getDataFilePath());
		try {
			Document doc = XmlBase.load(xmlFile, "guilds");
			Element eGuild = doc.createElement("guild");
			eGuild.setAttribute("id", newGuild.getID() + "");
			eGuild.appendChild(doc.createElement("specialCode"));
			eGuild.appendChild(doc.createElement("specialRole"));
			eGuild.appendChild(doc.createElement("auditChannel"));
			//eGuild.appendChild(doc.createElement("eventAuditChannel"));
			eGuild.appendChild(doc.createElement("specialCodeUsers"));
			Element rootElement = doc.getDocumentElement();
			rootElement.appendChild(eGuild);
			XmlBase.save(doc, xmlFile);
			
		} catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeSpecialCodeUser(Member member) {
		File xmlFile = new File(DiscordBot.INSTANCE.getDataFilePath());
		try {
			Document doc = XmlBase.load(xmlFile, "guilds");
			Element eGuild = getElementById(doc, member.getGuild().getIdLong(), "guild");	
			Element eUser = getElementById(doc, member.getIdLong(), "user");
			try {
				if(eUser == null) {			
					eGuild.getElementsByTagName("specialCodeUsers").item(0).appendChild(createUser(doc, member));
				}
        	}
        	catch (NullPointerException e) {
        		
        		Element eSpecialCodeUsers = doc.createElement("specialCodeUsers");
        		eGuild.appendChild(eSpecialCodeUsers);
        		eSpecialCodeUsers.appendChild(createUser(doc, member));
        	}
			
			XmlBase.save(doc, xmlFile);
			
		} catch (SAXException | IOException | ParserConfigurationException | TransformerException | NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void writeSpecialCode(String code, long guildId) {
		writeToElement(code, guildId, "specialCode");
	}
	
	public static void writeSpecialCodeRole(long roleId, long guildId) {
		writeToElement(roleId + "", guildId, "specialRole");
	}
	
	public static void writeAuditChannel(long channelId, long guildId) {
		writeToElement(channelId + "", guildId, "auditChannel");
	}
	
	/*public static void writeEventAuditChannel(long channelId, long guildId) {
		writeToElement(channelId + "", guildId, "eventAuditChannel");
	}*/
	
	private static void writeToElement(String content, long guildId, String elementName) {
		File xmlFile = new File(DiscordBot.INSTANCE.getDataFilePath());
		
		try {
			Document doc = XmlBase.load(xmlFile, "guilds");
			
			Element eGuild = getElementById(doc, guildId, "guild");
		        	
				
        	try {
        		eGuild.getElementsByTagName(elementName).item(0).setTextContent(content);
        	}
        	catch (NullPointerException e) {
        		
        		Element eCode = doc.createElement(elementName);
        		eCode.setTextContent(content);
        		try {
        			eGuild.appendChild(eCode);
        		} catch(NullPointerException e2) {
        			System.err.println("Atention! " + DiscordBot.INSTANCE.getDataFilePath() + " probably deleted during runtime. Restart Bot!");
        		}
        	}
			XmlBase.save(doc, xmlFile);
			
		} catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}
	}

	private static Element getElementById(Document doc, long wantedId, String elementName) {
		NodeList nElements = doc.getElementsByTagName(elementName);
		for(int i = 0; i < nElements.getLength(); i ++) {
        	Node nElement = nElements.item(i);
    		Element eElement = (Element) nElement;
    		Long guildId = Long.parseLong(eElement.getAttribute("id"));
    		
    		if (wantedId == guildId) {
    			return eElement;
    		}
		}
		return null;
	}
	
	public static String readSpecialCode(long guildId) {
		return readSingleElement(guildId, "specialCode");
	}
	
	public static long readSpecialCodeRole(Long guildId) {
		String code = readSingleElement(guildId, "specialRole");
		if(code.equals("")) {
			return 0;
		}
		else {
			return Long.parseLong(code);
		}
	}
	
	public static long readAuditChannelId(Long guildId) {
		String id = readSingleElement(guildId, "auditChannel");
		if(id.equals("")) {
			return 0;
		}
		else {
			return Long.parseLong(id);
		}
	}
	
	/*public static long readEventAuditChannelId(Long guildId) {
		String id = readSingleElement(guildId, "eventAuditChannel");
		if(id.equals("")) {
			return 0;
		}
		else {
			return Long.parseLong(id);
		}
	}*/
	
	private static String readSingleElement(long guildId, String elementName) {
		File xmlFile = new File(DiscordBot.INSTANCE.getDataFilePath());
		
		try {
			Document doc = XmlBase.load(xmlFile, "guilds");
		    Element eGuild = getElementById(doc, guildId, "guild");
			Node code = eGuild.getElementsByTagName(elementName).item(0);
    		return code.getTextContent();
					
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.err.println(elementName + " wurde noch nicht zugewiesen (GuildDataXmlReadWrite.java - readSingleElement)");
		}
		return "";
	}
	
	public static List<GuildData> readGuildData()  {
		List<GuildData> guildsData = new ArrayList<>();
		File xmlFile = new File(DiscordBot.INSTANCE.getDataFilePath());
		try {
			Document doc = XmlBase.load(xmlFile, "guilds");
			NodeList nGuilds = doc.getElementsByTagName("guild");
			
			for(int i = 0; i < nGuilds.getLength(); i ++) {
	        	Node nGuild = nGuilds.item(i);
        		Element eGuild = (Element) nGuild;
        		String guildID = eGuild.getAttribute("id");
        		GuildData newGuildData = new GuildData(Long.parseLong(guildID));
        		
        		try {
        			String inviteCode = eGuild.getElementsByTagName("specialCode").item(0).getTextContent();
        			newGuildData.setSpecialInviteCode(inviteCode);
        		}
        		catch(NullPointerException e) {
        			
        		}
        		
        		try {
        			long specialCodeID = Long.parseLong(eGuild.getElementsByTagName("specialCodeRole").item(0).getTextContent());
        			newGuildData.setSpecialRoleID(specialCodeID);
        		}
        		catch(NullPointerException e) {
        			
        		}
        		guildsData.add(newGuildData);
			}
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		return guildsData;
	}
	
	private static Element createUser(Document doc, Member member) {
		Element eUser = doc.createElement("user");
		eUser.setAttribute("id", member.getId());
		return eUser;
	}
}
