package serverClient;

import security.DigitalSignature;
import security.HyperEncryption;
import security.SymmetricEncryption;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;


public class Client implements Runnable
{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private KeyPair keyPair;
    private PublicKey serverPublicKey;



    private PublicKey init() throws Exception
    {
        // receive server public key
        ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
        PublicKey key = (PublicKey) objectInputStream.readObject();

        // send public key
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        objectOutputStream.writeObject(keyPair.getPublic());

        return key;
    }


    @Override
    public void run()
    {
        try
        {
            client = new Socket("localhost",4000);
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            keyPair =  DigitalSignature.generateKeyPair();
            System.out.println("The Public Key is: " + DatatypeConverter.printHexBinary(keyPair.getPublic().getEncoded()));
            System.out.println("The Private Key is: " + DatatypeConverter.printHexBinary(keyPair.getPrivate().getEncoded()));

            serverPublicKey = init();
            System.out.println("The Server Public Key is: " + DatatypeConverter.printHexBinary(serverPublicKey.getEncoded()));


            InputHandler inHandler = new InputHandler();
            Thread thread = new Thread(inHandler);
            thread.start();

            String serverResponse;
            while ((serverResponse = in.readLine()) != null)
            {
                System.out.println(HyperEncryption.Decrept(serverResponse,keyPair.getPrivate()));
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
        catch (IOException e){/* ignore */}
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
                   String request = inReader.readLine();
                   if(request.equals("/quit"))
                   {
                       out.println(request);
                       //out.println(SymmetricEncryption.encrypt(message,symmetricKey));
                       inReader.close();
                       ShutDowm();
                   }
                   else
                   {
                       String strDs =  DigitalSignature.createDigitalSignature(request.getBytes(),keyPair.getPrivate());
                       String strMess = HyperEncryption.Encrept(request ,serverPublicKey);
                       out.println(strMess + "DS" + strDs);
                   }
               }
           }
           catch (Exception e)
           {
               e.printStackTrace();
               ShutDowm();
           }
        }
    }

    public static void main(String[] args)
    {
        new Client().run();
    }
}
