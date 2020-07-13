package de.java.main.files;
import java.io.*;
import java.util.*;

public class PropertiesReader {
	
	private String botToken = "yourBotToken";
	private String dataFilePath = "./data.xml";
	private String autoResponsePath = "./autoresponse.txt";
	private String[] defIds;
	private String defIdsString = "id1,id2,id3,...";
	
	public PropertiesReader() throws IOException {
		Properties props = new Properties();
		try (InputStream inStream = new FileInputStream(".\\config.properties")) {
			props.load(inStream);
			try (OutputStream output = new FileOutputStream(".\\config.properties")) {
				if(props.containsKey("botToken"))
					botToken = props.getProperty("botToken");
				else
					props.setProperty("botToken", botToken);
				
				if(props.containsKey("dataFilePath"))
					dataFilePath = props.getProperty("dataFilePath");
				else
					props.setProperty("dataFilePath", dataFilePath);
				
				if(props.containsKey("autoResponsePath"))
					autoResponsePath = props.getProperty("autoResponsePath");
				else
					props.setProperty("autoResponsePath", autoResponsePath);
				
				if(props.containsKey("defIds"))
					defIdsString = props.getProperty("defIds");
				else
					props.setProperty("defIds", defIdsString);
				
				props.store(output, null);
				
			} catch (IOException io) {
	            io.printStackTrace();
	        }
			
			defIds = stringToArray(defIdsString);
			dataFilePath = correctPathString(dataFilePath);
			autoResponsePath = correctPathString(autoResponsePath);
			
		} catch (FileNotFoundException e) {
			createNewPropertiesFile();
		}
	}
	
	private void createNewPropertiesFile(){
		try (OutputStream output = new FileOutputStream(".\\config.properties")) {
			botToken = "yourBotToken";
			dataFilePath = "./data.xml";
			autoResponsePath = "./autoresponse.txt";
            Properties props = new Properties();
            props.setProperty("botToken", botToken);
            props.setProperty("dataFilePath", dataFilePath);
            props.setProperty("autoResponsePath", autoResponsePath);
            props.setProperty("defIds", defIdsString);
            props.store(output, null);
            System.out.println("config.properties created");

        } catch (IOException io) {
            io.printStackTrace();
        }
	}

	private String[] stringToArray(String s) {
		s = s.replace(" ", "");
		return s.split(",");
	}
	
	private String correctPathString(String path) {
		path = path.replaceAll("/", "\\\\");
		if(!String.valueOf(path.charAt(0)).equals(".")) {
			path = "." + path;
		}
		return path;
	}
	
	public String getBotToken() {
		return botToken;
	}

	public String getDataFilePath() {
		return dataFilePath;
	}

	public String getAutoResponsePath() {
		return autoResponsePath;
	}
	
	public String[] getDefIds() {
		return defIds;
	}
}