package security;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SymmetricEncryption
{
    private static final String key = "aesEncryptionKey";

    public static SecretKey createAESKey(String password) throws Exception
    {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec passwordBasedEncryptionKeySpec = new PBEKeySpec(password.toCharArray(), key.getBytes(), 12288, 128);
        SecretKey secretKeyFromPBKDF2 = secretKeyFactory.generateSecret(passwordBasedEncryptionKeySpec);
        SecretKey symmetricKey = new SecretKeySpec(secretKeyFromPBKDF2.getEncoded(), "AES");
        return symmetricKey;
    }


    public static SecretKey createAESKey() throws Exception
    {
        SecureRandom securerandom = new SecureRandom();
        KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
        keygenerator.init(256, securerandom);
        SecretKey key = keygenerator.generateKey();
        return key;
    }


    public static SecretKey GenerateSessionKey() throws NoSuchAlgorithmException
    {
        KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
        SecretKey symmetricKey= keygenerator.generateKey();
        return symmetricKey;
    }


    public static String MAC(String request,SecretKey symmetricKey) throws Exception
    {
        Mac mac = Mac.getInstance("HMACSHA1");
        mac.init(symmetricKey);
        mac.update(request.getBytes());
        byte[] macResult = mac.doFinal();
        return java.util.Arrays.toString(macResult);
    }


    public static String encrypt(String data,SecretKey symmetricKey)
    {
        String encryptData = "";
        try
        {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            encryptData = Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return encryptData;
    }

    public static String decrypt(String data,SecretKey symmetricKey)
    {
        byte[] encrypted = Base64.getDecoder().decode(data);
        String decryptData = "";
        try
        {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, symmetricKey);
            byte[] decrypted = cipher.doFinal(encrypted);
            decryptData = new String(decrypted, StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return decryptData;
    }



    public static void main(String args[]) throws Exception
    {
        SecretKey symmetricKey1 = createAESKey("0990");
        SecretKey symmetricKey2 = createAESKey("0990");

        String message = "/login-mohamd-0990";

        String encryptData = encrypt(message,symmetricKey1);
        System.out.println("encryptData : " +  encryptData);
        String Mac1 = MAC(message,symmetricKey1);
        System.out.println("Mac send : " +  Mac1);

        String decryptDate = decrypt(encryptData,symmetricKey2)+"1";
        System.out.println("decrypt : "     +  decryptDate);
        String Mac2 = MAC(decryptDate,symmetricKey2);
        System.out.println("Mac receive : " +  Mac2);
    }
}
