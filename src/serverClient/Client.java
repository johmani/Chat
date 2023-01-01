package serverClient;

import security.Hyper;
import security.SymmetricEncryption;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.PublicKey;


public class Client implements Runnable
{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private SecretKey symmetricKey;

    public void handShaking() throws Exception
    {
        // receive server public key
        ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
        PublicKey serverPublicKey = (PublicKey) objectInputStream.readObject();
        System.out.println("The Public Key is: " + DatatypeConverter.printHexBinary(serverPublicKey.getEncoded()));

        // Generate session Key
        symmetricKey = SymmetricEncryption.GenerateSessionKey();
        System.out.println("The Session Key is :" + DatatypeConverter.printHexBinary(symmetricKey.getEncoded()));

        // Encrypt session key
        byte[] EncryptedKey = Hyper.Encrept(DatatypeConverter.printHexBinary(symmetricKey.getEncoded()),serverPublicKey);

        //send session key
        System.out.println("The Encrypted Session Key is :" + DatatypeConverter.printHexBinary(EncryptedKey));
        out.println(DatatypeConverter.printHexBinary(EncryptedKey));
    }


    @Override
    public void run()
    {
        try
        {
            client = new Socket("localhost",4000);
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            handShaking();


            InputHandler inHandler = new InputHandler();
            Thread thread = new Thread(inHandler);
            thread.start();

            String serverResponse;
            while ((serverResponse = in.readLine()) != null)
            {
                String response = serverResponse.split("mac")[0];
                String decrypt = SymmetricEncryption.decrypt(response,symmetricKey);

                String mac = SymmetricEncryption.MAC(decrypt,symmetricKey);
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
                       out.println(SymmetricEncryption.encrypt(message,symmetricKey));
                       inReader.close();
                       ShutDowm();
                   }
                   else
                   {
                       out.println(SymmetricEncryption.encrypt(message,symmetricKey));
                   }
               }
           }
           catch (Exception e)
           {
               ShutDowm();
           }
        }
    }

    public static void main(String[] args)
    {
        new Client().run();
    }
}
