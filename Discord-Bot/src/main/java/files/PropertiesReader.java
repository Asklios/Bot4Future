package main.java.files;

import java.io.*;
import java.util.Properties;

public class PropertiesReader {

    private String botToken = "yourBotToken";
    private String dataFilePath = "data.xml";
    private String autoResponsePath = "autoresponse.txt";
    private String logFilePath = "log.txt";
    private String diagramFilePath = "diagram.png";
    private String pbFilterPath = "pbfilter.png";
    private String pbPath = "pb.png";
    private String newPbPath = "newPb.png";
    private String dbFilePath = "database.db";
    private String[] defIds;
    private String defIdsString = "id1,id2,id3,...";

    public PropertiesReader() throws IOException {
        Properties props = new Properties();
        try (InputStream inStream = new FileInputStream("config.properties")) {
            props.load(inStream);
            try (OutputStream output = new FileOutputStream("config.properties")) {
                if (props.containsKey("botToken"))
                    botToken = props.getProperty("botToken");
                else
                    props.setProperty("botToken", botToken);

                if (props.containsKey("dataFilePath"))
                    dataFilePath = props.getProperty("dataFilePath");
                else
                    props.setProperty("dataFilePath", dataFilePath);

                if (props.containsKey("autoResponsePath"))
                    autoResponsePath = props.getProperty("autoResponsePath");
                else
                    props.setProperty("autoResponsePath", autoResponsePath);

                if (props.containsKey("dbFilePath"))
                    dbFilePath = props.getProperty("dbFilePath");
                else
                    props.setProperty("dbFilePath", dbFilePath);

                if (props.containsKey("defIds"))
                    defIdsString = props.getProperty("defIds");
                else
                    props.setProperty("defIds", defIdsString);

                if (props.containsKey("logFilePath"))
                    logFilePath = props.getProperty("logFilePath");
                else
                    props.setProperty("logFilePath", logFilePath);

                if (props.containsKey("diagramFilePath"))
                    diagramFilePath = props.getProperty("diagramFilePath");
                else
                    props.setProperty("diagramFilePath", diagramFilePath);

                if (props.containsKey("pbfilter.png"))
                    pbFilterPath = props.getProperty("pbfilter.png");
                else
                    props.setProperty("pbfilter.png", pbFilterPath);

                if (props.containsKey("pb.png"))
                    pbPath = props.getProperty("pb.png");
                else
                    props.setProperty("pb.png", pbPath);

                if (props.containsKey("newPb.png"))
                    newPbPath = props.getProperty("newPb.png");
                else
                    props.setProperty("newPb.png", newPbPath);

                props.store(output, null);

            } catch (IOException io) {
                io.printStackTrace();
            }

            defIds = stringToArray(defIdsString);
            dataFilePath = correctPathString(dataFilePath);
            autoResponsePath = correctPathString(autoResponsePath);
            logFilePath = correctPathString(logFilePath);
            diagramFilePath = correctPathString(diagramFilePath);
            pbFilterPath = correctPathString(pbFilterPath);
            pbPath = correctPathString(pbPath);
            newPbPath = correctPathString(newPbPath);
            dbFilePath = correctPathString(dbFilePath);

        } catch (FileNotFoundException e) {
            createNewPropertiesFile();
        }
    }

    private void createNewPropertiesFile() {
        try (OutputStream output = new FileOutputStream("config.properties")) {
            botToken = "yourBotToken";
            dataFilePath = "data.xml";
            autoResponsePath = "autoresponse.txt";
            Properties props = new Properties();
            props.setProperty("botToken", botToken);
            props.setProperty("dataFilePath", dataFilePath);
            props.setProperty("autoResponsePath", autoResponsePath);
            props.setProperty("dbFilePath", dbFilePath);
            props.setProperty("defIds", defIdsString);
            props.setProperty("logFilePath", logFilePath);
            props.setProperty("diagramFilePath", diagramFilePath);
            props.setProperty("pbfilter.png", pbFilterPath);
            props.setProperty("pb.png", pbPath);
            props.setProperty("newPb.png", newPbPath);
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
		/*
		path = path.replaceAll("/", "\\\\");
		if(!String.valueOf(path.charAt(0)).equals(".")) {
			path = "." + path;
		}*/
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

    public String getDbFilePath() {
        return dbFilePath;
    }

    public String[] getDefIds() {
        return defIds;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public String getDiagramFilePath() {return diagramFilePath;}

    public String getPbFilterPath() {return pbFilterPath;}
    public String getPbPath() {return pbPath;}
    public String getNewPbPath() {return newPbPath;}
}