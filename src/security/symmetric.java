package security;

//import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security
        .SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;
import java.lang.Object;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.print.DocFlavor;
import javax.xml.bind.DatatypeConverter;
//import javax.xml.bind.DatatypeConverter;

public class symmetric
{
    static GCMParameterSpec spec;
    static SecretKey SymmetricKey;
    private static final String key = "aesEncryptionKey";

    // Function to create a secret key
    public static SecretKey createAESKey(String password) throws Exception
    {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec passwordBasedEncryptionKeySpec = new PBEKeySpec(password.toCharArray(), key.getBytes(), 12288, 128);
        SecretKey secretKeyFromPBKDF2 = secretKeyFactory.generateSecret(passwordBasedEncryptionKeySpec);
        SymmetricKey = new SecretKeySpec(secretKeyFromPBKDF2.getEncoded(), "AES");
        return SymmetricKey;
    }

    public static SecretKey  GenerateSessionKey() throws NoSuchAlgorithmException
    {
        KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
        SymmetricKey= keygenerator.generateKey();
        return SymmetricKey;
    }

    public static GCMParameterSpec getGCMParameterSpec(byte[] nonce)
    {
        GCMParameterSpec spec = new GCMParameterSpec(16 * 8, nonce);
        return spec;
    }

    public static byte[] MAC(ArrayList request) throws Exception
    {
        Mac mac = Mac.getInstance("HMACSHA1");
        mac.init(SymmetricKey);
        mac.update(request.toString().getBytes());
        byte[] macResult = mac.doFinal();
        //   System.out.println(java.util.Arrays.toString(mac.doFinal()));
        return macResult;
    }


    public static String encrypt(String data, GCMParameterSpec GCM)
    {
        String encryptData = "";
        try
        {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, SymmetricKey);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            encryptData = Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return encryptData;
    }

    public static String decrypt(String data, GCMParameterSpec GCM)
    {
        byte[] encrypted = Base64.getDecoder().decode(data);
        String decryptData = "";

        try
        {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, SymmetricKey);
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
        SecretKey Symmetrickey = GenerateSessionKey();
        System.out.println("The Symmetric Key is :" + DatatypeConverter.printHexBinary(Symmetrickey.getEncoded()));
        SecureRandom random = SecureRandom.getInstanceStrong();
        final byte[] nonce = new byte[32];
        random.nextBytes(nonce);
        spec = getGCMParameterSpec(nonce);

        String encryptData=encrypt("mohamd",spec);

        System.out.println("encryptData : " +  encryptData);
        System.out.println("decrypt : "     +  decrypt(encryptData,spec));
    }
}
