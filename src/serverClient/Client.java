package serverClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
                   else
                   {
                       out.println(message);
                   }
               }
           }
           catch (IOException e)
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
