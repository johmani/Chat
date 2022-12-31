package serverClient;
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

            String inMessage;
            while ((inMessage = in.readLine()) != null)
            {
                System.out.println(inMessage);
            }
        }
        catch (IOException e)
        {
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
        private SecretKey symmetricKey;


        @Override
        public void run()
        {
           try
           {
               inReader = new BufferedReader(new InputStreamReader(System.in));

               ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
               PublicKey serverPublicKey = (PublicKey) objectInputStream.readObject();


               System.out.println("The Public Key is: " + DatatypeConverter.printHexBinary(serverPublicKey.getEncoded()));

//               // Generate session Key
//               symmetricKey = symmetric.GenerateSessionKey();
//               System.out.println("The Session Key is :" + DatatypeConverter.printHexBinary(symmetricKey.getEncoded()));
//
//               // Encrypt session key
//               byte[] EncryptedKey = Hyper.Encrept(DatatypeConverter.printHexBinary(symmetricKey.getEncoded()),serverpublickey);
//
//               //send session key
//               System.out.println("The Encrypted Session Key is :" + DatatypeConverter.printHexBinary(EncryptedKey));
//               out.println(DatatypeConverter.printHexBinary(EncryptedKey));

               while (!done)
               {
                   String  message = inReader.readLine();
                   if(message.equals("/quit"))
                   {
                       out.println(message);
                       inReader.close();
                       ShutDowm();
                   }
                   else
                   {
                       out.println(message);
                   }
               }
           }
           catch (IOException | ClassNotFoundException e)
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
