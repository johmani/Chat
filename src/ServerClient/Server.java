package ServerClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable
{
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server()
    {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run()
    {
        try
        {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();

            while (!done)
            {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        }
        catch (Exception e)
        {
           ShutDown();
        }
    }

    public void BroadCast(String message)
    {
        for(ConnectionHandler ch : connections)
        {
            if(ch != null)
            {
                ch.SendMessage(message);
            }
        }
    }

    public void ShutDown()
    {
        try
        {
            done = true;
            pool.shutdown();
            if (!server.isClosed())
            {
                server.close();
            }
            for(ConnectionHandler ch : connections)
            {
                ch.ShutDown();
            }
        }
        catch (IOException e)
        {
           // ignore
        }
    }

    class  ConnectionHandler implements Runnable
    {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickName;

        public ConnectionHandler(Socket client)
        {
            this.client = client;
        }

        @Override
        public void run()
        {
            try
            {
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Enter a nickName : ");
                nickName = in.readLine();
                System.out.println(nickName + " connected!");

                BroadCast(nickName + " joined the chat.");

                String message;
                while ((message = in.readLine()) != null)
                {
                    if(message.startsWith("/nick "))
                    {
                        String[] messageSpilt = message.split(" ",2);
                        if(messageSpilt.length == 2)
                        {
                            BroadCast(nickName + " renamed themselves to " + messageSpilt[1]);
                            System.out.println(nickName + " renamed themselves to " + messageSpilt[1]);
                            nickName = messageSpilt[1];
                            out.println("Successfully changed nickname to " + nickName);
                        }
                        else
                        {
                            out.println("No nick provided.");
                        }
                    }
                    else if(message.startsWith("/quit"))
                    {
                        BroadCast(nickName + " left the chat.");
                        ShutDown();
                    }
                    else
                    {
                        BroadCast(nickName + " : " + message);
                    }
                }
            }
            catch (IOException e)
            {
                ShutDown();
            }
        }

        public void SendMessage(String message)
        {
            out.println(message);
        }


        public void ShutDown()
        {
            try
            {
                in.close();
                out.close();
                if (!client.isClosed())
                {
                    client.close();
                }
            }
            catch (IOException e)
            {
                // ignore
            }
        }
    }

    public static void main(String[] args)
    {
        new Server().run();
    }
}
