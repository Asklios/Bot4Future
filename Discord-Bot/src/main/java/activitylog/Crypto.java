package main.java.activitylog;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Crypto {

    public byte[] encryptText (String text, String password) {

        byte[] cryptoText;

        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] pwBytes = password.getBytes(StandardCharsets.UTF_8);

        SecretKeySpec key = new SecretKeySpec(pwBytes, "AES");

        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] crypto = new byte[cipher.getOutputSize(textBytes.length)];
            int enc_len = cipher.update(textBytes, 0, textBytes.length, crypto, 0);
            try {
                enc_len += cipher.doFinal(crypto, enc_len);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }
            return crypto;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                ShortBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decryptText (byte[] crypto, String password) {

        byte[] pwBytes = password.getBytes(StandardCharsets.UTF_8);

        SecretKeySpec key = new SecretKeySpec(pwBytes, "AES");

        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptText = new byte[cipher.getOutputSize(crypto.length)];
            int dec_len = cipher.update(crypto, 0, crypto.length, decryptText, 0);
            dec_len += cipher.doFinal(decryptText, dec_len);
            return new String(decryptText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                ShortBufferException | IllegalBlockSizeException |
                BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
