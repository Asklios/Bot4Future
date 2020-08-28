package main.java.files;

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

import main.java.DiscordBot;
import main.java.GuildData;
import net.dv8tion.jda.api.entities.Member;

public class GuildDataXmlReadWrite {
	
	public static void writeNewGuild(GuildData newGuild) {
		File xmlFile = new File(DiscordBot.INSTANCE.getDataFilePath());
		try {
			Document doc = XmlBase.load(xmlFile, "guilds");
			Element eGuild = doc.createElement("guild");
			eGuild.setAttribute("id", newGuild.getID() + "");
			Element inv = doc.createElement("inviteManager");
			eGuild.appendChild(inv);
			inv.appendChild(doc.createElement("specialCode"));
			inv.appendChild(doc.createElement("specialRole"));
			inv.appendChild(doc.createElement("verifiableRole"));
			inv.appendChild(doc.createElement("specialCodeUsers"));
			eGuild.appendChild(doc.createElement("auditChannel"));
			//eGuild.appendChild(doc.createElement("eventAuditChannel"));
			eGuild.appendChild(doc.createElement("pnChannel"));
			Element eMod = doc.createElement("moderation");
			eGuild.appendChild(eMod);
			eMod.appendChild(doc.createElement("bans"));
			eMod.appendChild(doc.createElement("unbans"));
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
	
	public static void writeVerifiedUser(Member member, Member verMember) {
		File xmlFile = new File(DiscordBot.INSTANCE.getDataFilePath());
		try {
			Document doc = XmlBase.load(xmlFile, "guilds");
			Element eGuild = getElementById(doc, member.getGuild().getIdLong(), "guild");
			
			Element eSpecialCodeUsers = (Element) eGuild.getElementsByTagName("specialCodeUsers").item(0);

			Element eUser = (Element) getUserById(member.getIdLong(), eGuild, eSpecialCodeUsers);
			
			if(eUser == null) {
				eUser = createUser(doc, member);
				eSpecialCodeUsers.appendChild(eUser);
			}
			if(!eUser.getParentNode().equals(eSpecialCodeUsers)) {
				eSpecialCodeUsers.appendChild(eUser);
			} 
			eUser.appendChild(createUser(doc, verMember));
			
			
			XmlBase.save(doc, xmlFile);
			
		} catch (SAXException | IOException | ParserConfigurationException | TransformerException | NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeBan(long userId, long guildId) {
		removeUserFromModerationElement(userId, guildId, "unbans");
		File xmlFile = new File(DiscordBot.INSTANCE.getDataFilePath());
		
		try {
			Document doc = XmlBase.load(xmlFile, "guilds");
			
			Element eGuild = getElementById(doc, guildId, "guild");
        	if(eGuild.getElementsByTagName("bans").getLength() == 0) {
        		if(eGuild.getElementsByTagName("moderation").getLength() != 0) {
        			eGuild.getElementsByTagName("moderation").item(0).appendChild(doc.createElement("bans"));
        		} else {
        			Element eMod = doc.createElement("moderation");
        			eGuild.appendChild(eMod);
        			eMod.appendChild(doc.createElement("bans"));
        		}
        	}
        	Node nBans = eGuild.getElementsByTagName("bans").item(0);
        	Element eUser = doc.createElement("user");
    		eUser.setAttribute("id", "" + userId);
    		
        	Element eRequest = doc.createElement("request");
        	eRequest.setTextContent("false");
        	eUser.appendChild(eRequest);
        	nBans.appendChild(eUser);
			XmlBase.save(doc, xmlFile);
			
		} catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeUnban(long userId, long guildId) {
		removeUserFromModerationElement(userId, guildId, "bans");
		File xmlFile = new File(DiscordBot.INSTANCE.getDataFilePath());
		
		try {
			Document doc = XmlBase.load(xmlFile, "guilds");
			
			Element eGuild = getElementById(doc, guildId, "guild");
			if(eGuild.getElementsByTagName("unbans").getLength() == 0) {
        		if(eGuild.getElementsByTagName("moderation").getLength() != 0) {
        			eGuild.getElementsByTagName("moderation").item(0).appendChild(doc.createElement("unbans"));
        		} else {
        			Element eMod = doc.createElement("moderation");
        			eGuild.appendChild(eMod);
        			eMod.appendChild(doc.createElement("unbans"));
        		}
        	}
			
			Node nUnbans = eGuild.getElementsByTagName("unbans").item(0);
        	Element eUser = doc.createElement("user");
    		eUser.setAttribute("id", "" + userId);
    		nUnbans.appendChild(eUser);
			XmlBase.save(doc, xmlFile);
		} catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeUnbanRequestValue(long userId, long guildId, boolean value) {
		File xmlFile = new File(DiscordBot.INSTANCE.getDataFilePath());
		
		try {
			Document doc = XmlBase.load(xmlFile, "guilds");
			Element eGuild = getElementById(doc, guildId, "guild");
			if(eGuild.getElementsByTagName("bans").getLength() != 0) {
				Element eUser = (Element) getUserById(userId, eGuild, (Element) eGuild.getElementsByTagName("bans").item(0));
				eUser.getElementsByTagName("request").item(0).setTextContent("" + value);
        		XmlBase.save(doc, xmlFile);
        				        	}
		} catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean readUnbanRequestValue(long userId, long guildId) {
		File xmlFile = new File(DiscordBot.INSTANCE.getDataFilePath());
		
		try {
			Document doc = XmlBase.load(xmlFile, "guilds");
		    Element eGuild = getElementById(doc, guildId, "guild");
		    if(eGuild.getElementsByTagName("bans").getLength() > 0) {
		    	
		    	Element eUser = (Element) getUserById(userId, eGuild, (Element) eGuild.getElementsByTagName("bans").item(0));
		    	String value = eUser.getElementsByTagName("request").item(0).getTextContent();
		    	if(value.equals("true"))
		    		return true;
		    	else if(value.equals("false"))
		    		return false;
   		    }
					
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static Node getUserById(long userId, Element eGuild, Element container) {
		NodeList nUsers = container.getElementsByTagName("user");
		if(nUsers.getLength() > 0) {
			for(int i = 0; i < nUsers.getLength(); i ++) {
				if(Long.parseLong(((Element) nUsers.item(i)).getAttribute("id")) == userId) {
					return nUsers.item(i);
				}
			}
		}
		return null;
	}
	
	private static void removeUserFromModerationElement(long userId, long guildId, String elementName) {
		File xmlFile = new File(DiscordBot.INSTANCE.getDataFilePath());
		
		try {
			Document doc = XmlBase.load(xmlFile, "guilds");
			
			Element eGuild = getElementById(doc, guildId, "guild");
        	if(eGuild.getElementsByTagName(elementName).getLength() != 0) {
        		Node node = eGuild.getElementsByTagName(elementName).item(0);
        		Element element = (Element) node;
        		element.removeChild(getUserById(userId, eGuild, element));
        	}
        	XmlBase.save(doc, xmlFile);
		} catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
			e.printStackTrace();
		}
		catch (NullPointerException e) {
			
		}
	}
	
	public static void writeSpecialCode(String code, long guildId) {
		writeToElement(code, guildId, "specialCode");
	}
	
	public static void writeSpecialCodeRole(long roleId, long guildId) {
		writeToElement(roleId + "", guildId, "specialRole");
	}
	
	public static void writeVerifiableRole(long roleId, long guildId) {
		writeToElement(roleId + "", guildId, "verifiableRole");
	}
	
	public static void writeAuditChannel(long channelId, long guildId) {
		writeToElement(channelId + "", guildId, "auditChannel");
	}
	
	/*public static void writeEventAuditChannel(long channelId, long guildId) {
		writeToElement(channelId + "", guildId, "eventAuditChannel");
	}*/
	
	public static void writePnChannel(long channelId, long guildId) {
		writeToElement(channelId + "", guildId, "pnChannel");
	}
	
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
	
	public static long readVerifiableRole(Long guildId) {
		String code = readSingleElement(guildId, "verifiableRole");
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
	
	public static long readPnChannelId(Long guildId) {
		String id = readSingleElement(guildId, "pnChannel");
		if(id.equals("")) {
			return 0;
		}
		else {
			return Long.parseLong(id);
		}
	}
	
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
