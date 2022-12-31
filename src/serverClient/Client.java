package serverClient;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import java.util.Base64;

public class Client implements Runnable
{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private boolean loggedin;
    private SecretKey symmetricKey;

    @Override
    public void run()
    {
        try
        {
            client = new Socket("localhost",4000);
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread thread = new Thread(inHandler);
            thread.start();

            String serverResponse;

            while((serverResponse = in.readLine()) != null)
            {
                if(serverResponse.startsWith("Successfully logged in"))
                {
                    loggedin = true;
                    String password = serverResponse.split(",")[1];
                    symmetricKey = Symmetric.createAESKey(password);
                    System.out.println(serverResponse);
                }
                else if(loggedin)
                {
                    String response = serverResponse.split("mac")[0];
                    String decrypt = Symmetric.decrypt(response,symmetricKey);

                    String mac = Symmetric.MAC(decrypt,symmetricKey);
                    String receivedMac = serverResponse.split("mac")[1];

                    if(mac.equals(receivedMac))
                    {
                        System.out.println("correct mac : "+ decrypt);
                    }
                    else
                    {
                        System.out.println("uncorrect mac : "+ decrypt);
                    }
                }
                else
                {
                    System.out.println(serverResponse);
                }
            }
        }
        catch (Exception e)
        {
           e.printStackTrace();
           ShutDowm();
        }
    }

    public void ShutDowm()
    {
        done = true;
        try
        {
            in.close();
            out.close();
            if (!client.isClosed())  client.close();
        }
        catch (IOException e){ /* ignore */}
    }

    class InputHandler implements Runnable
    {
        BufferedReader inReader;

        @Override
        public void run()
        {
           try
           {
               inReader = new BufferedReader(new InputStreamReader(System.in));

               while (!done)
               {
                   String  message = inReader.readLine();
                   if(message.equals("/quit"))
                   {
                       out.println(message);
                       inReader.close();
                       ShutDowm();
                   }
                   else if(message.startsWith("/login") || message.startsWith("/signup"))
                   {
                       out.println(message);
                   }
                   else
                   {
                       if(loggedin)
                       {
                           out.println(Symmetric.encrypt(message,symmetricKey));
                       }
                       else
                       {
                           System.out.println("you must login first , use /login-number-password to login");
                       }
                   }
               }
           }
           catch (IOException e)
           {
               ShutDowm();
           }
           catch (Exception e)
           {
               e.printStackTrace();
           }
        }
    }

    public static void main(String[] args)
    {
        new Client().run();
    }
}


class Symmetric
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


    public static String MAC(String request, SecretKey symmetricKey) throws Exception
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
//      System.out.println("symmetric key :" + DatatypeConverter.printHexBinary(symmetricKey.getEncoded()));

        String encryptData = encrypt("/login-mohamd-0990",symmetricKey1);
        String decryptDate = decrypt(encryptData,symmetricKey2);

        System.out.println("encryptData : " +  encryptData);
        System.out.println("decrypt : "     +  decryptDate);
    }
}
