package main.java.activitylog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CryptoMessageHandler {

    public void saveNewMessage(String message, long guildId, long channelId, long messageId, long userId) {

        String guildStr = "" + guildId;
        String channelStr = "" + channelId;

        String password = guildStr.substring(10) + channelStr.substring(10); //TODO AES key generation
        saveToDatabase(new Crypto().encryptText(userId + message, password), messageId);
    }

    public void updateMessage(String message, long guildId, long channelId, long messageId, long userId) {

        String guildStr = "" + guildId;
        String channelStr = "" + channelId;

        String password = guildStr.substring(10) + channelStr.substring(10); //TODO AES key generation
        updateDatabase(new Crypto().encryptText(userId + message, password), messageId);
    }

    public String readEncryptedMessage(long guildId, long channelId, long messageId) {
        String guildStr = "" + guildId;
        String channelStr = "" + channelId;

        String password = guildStr.substring(10) + channelStr.substring(10); //TODO AES key generation
        byte[] cryptoText = readFromDatabase(messageId);
        if (cryptoText == null) return null;
        return new Crypto().decryptText(cryptoText, password).substring(18);
    }

    public String readEncryptedMessageWithId(long guildId, long channelId, long messageId) {
        String guildStr = "" + guildId;
        String channelStr = "" + channelId;

        String password = guildStr.substring(10) + channelStr.substring(10); //TODO AES key generation
        byte[] cryptoText = readFromDatabase(messageId);
        if (cryptoText == null) return null;
        return new Crypto().decryptText(cryptoText, password);
    }

    /**
     * Saves an encrypted byte[] to the message table.
     *
     * @param encrypted byte[] of the encrypted message.
     * @param messageId identifier in the table.
     */
    private void saveToDatabase(byte[] encrypted, long messageId) {
        try {
            Connection connection = LiteSQLActivity.POOL.getConnection();
            PreparedStatement prepStmt = connection.prepareStatement("INSERT INTO messages(messageid, encrypted) VALUES (?,?)");

            assert prepStmt != null;
            prepStmt.setLong(1, messageId);
            prepStmt.setBytes(2, encrypted);
            prepStmt.executeUpdate();
            prepStmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an entry in the message table.
     *
     * @param encrypted byte[] of the encrypted message.
     * @param messageId identifier in the table.
     */
    private void updateDatabase(byte[] encrypted, long messageId) {
        try {
            Connection connection = LiteSQLActivity.POOL.getConnection();
            PreparedStatement prepStmt = connection.prepareStatement("UPDATE messages SET encrypted = ? WHERE messageid = ?");

            assert prepStmt != null;
            prepStmt.setBytes(1, encrypted);
            prepStmt.setLong(2, messageId);
            prepStmt.executeUpdate();
            prepStmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds a message entry with the provided id
     *
     * @param messageId identifier in the table.
     * @return first found byte[] if successfully, otherwise null.
     */
    private byte[] readFromDatabase(long messageId) {
        try {
            Connection connection = LiteSQLActivity.POOL.getConnection();
            PreparedStatement prepStmt = connection.prepareStatement("SELECT encrypted FROM messages WHERE messageid = ?");

            assert prepStmt != null;
            prepStmt.setLong(1, messageId);
            ResultSet result = prepStmt.executeQuery();

            if (result.next()) {
                byte[] ret = result.getBytes("encrypted");
                prepStmt.close();
                connection.close();
                return ret;
            }
            prepStmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
