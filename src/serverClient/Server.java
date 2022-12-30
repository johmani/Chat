package serverClient;

import dataBase.MySqlConnection;
import model.MessageModel;
import model.UserModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable
{
    private Hashtable<String,ConnectionHandler> connections;
    private ServerSocket server;
    private ExecutorService pool;
    private boolean done;

    public Server()
    {
        connections = new Hashtable<>();
        done = false;
    }

    @Override
    public void run()
    {
        try
        {
            server = new ServerSocket(4000);
            pool = Executors.newCachedThreadPool();

            while (!done)
            {
                Socket client = server.accept();
                System.out.println("New client connected "+ client.getInetAddress().getHostAddress());
                ConnectionHandler handler = new ConnectionHandler(client);

                int n = new Random().nextInt(1000000);
                handler.user.userNumber(Integer.toString(n));

                connections.put(handler.user.userNumber(),handler);
                pool.execute(handler);
            }
        }
        catch (Exception e)
        {
           shutDown();
        }
    }

    public void broadCast(String message)
    {
        for(ConnectionHandler ch : connections.values())
        {
            if(ch != null)
            {
                ch.sendMessage(message);
            }
        }
    }

    public void broadCast(String to, String message)
    {
        connections.get(to).sendMessage(message);
    }


    public void shutDown()
    {
        try
        {
            done = true;
            pool.shutdown();
            if (!server.isClosed())
            {
                server.close();
            }
            for(ConnectionHandler ch : connections.values())
            {
                ch.shutDown();
            }
            connections.clear();
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
        private UserModel user;
        private boolean logeden = false;

        public ConnectionHandler(Socket client)
        {
            this.client = client;
            user = new UserModel();
        }

        private void quitCMD(String request)
        {
            shutDown(user.userNumber());
            System.out.println(user.userNumber() + " has quit");
        }

        private void loginCMD(String request)
        {
            String[] req = request.split("-");
            if(req.length == 3)
            {
                String response = MySqlConnection.Login(req[1],req[2]);
                out.println(response);

                if(response.startsWith("Successfully"))
                {
                    logeden = true;
                    ConnectionHandler connection = connections.remove(user.userNumber());

                    user.userNumber(req[1]);
                    user.password(req[2]);

                    connections.put(user.userNumber(),connection);
                    System.out.println(user.userNumber() + " logged in Successfully");
                }
            }
            else
            {
                out.println("your request is invalid");
            }
        }

        private void signupCMD(String request)
        {
            String[] req = request.split("-");
            if(req.length == 3)
            {
                String number = req[1];
                String password = req[2];
                String response = MySqlConnection.Signup(number,password);
                out.println(response);
                System.out.println(number + " Register Successfully");
            }
            else
            {
                out.println("your request is invalid");
            }
        }

        private  void sendCMD(String request)
        {
            if(logeden)
            {
                String[] req = request.split("-");
                if(req.length == 3)
                {
                    String from = user.userNumber();
                    String to = req[1];
                    String message = req[2];

                    String  response = MySqlConnection.isRegister(to);

                    if(response.equals("isRegister"))
                    {

                        if(connections.get(to)!=null)
                        {
                           broadCast(to,from + " : " + message);
                           out.println("+"+ from + " : " + message);
                        }
                        else
                        {
                            out.println("-"+ from + " : " + message);
                        }
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
                        LocalDateTime now = LocalDateTime.now();
                        String chatName = from +","+ to;

                        MessageModel newMessage = new MessageModel(from,message,dtf.format(now),chatName);
                        MySqlConnection.sendMessage(newMessage);
                    }
                    else
                    {
                        out.println(response);
                    }
                }
                else
                {
                    out.println("your request is invalid");
                }
            }
            else
            {
                out.println("you must login first , use /login-number-password to login");
            }
        }

        private  void loadMessagesCMD(String request)
        {
           if(logeden)
           {
               String[] req = request.split("-");
               if(req.length == 2)
               {
                   String to = req[1];
                   String chat = user.userNumber()+","+to;
                   ArrayList<String> messages = MySqlConnection.getMessages(chat);
                   for(String message : messages)
                   {
                       out.println(message);
                   }
               }
           }
           else
           {
               out.println("You must login first , use /login-number-password to login");
           }
        }

        private  void loadConversationsCMD(String request)
        {
            if(logeden)
            {
                String[] req = request.split("-");
                if(req.length == 1)
                {
                    ArrayList<String> conversations = MySqlConnection.getConversations(user.userNumber());
                    for(String message : conversations)
                    {
                        out.println(message);
                    }
                }
                else
                {
                    out.println("your request is invalid");
                }
            }
            else
            {
                out.println("You must login first , use /login-number-password to login");
            }
        }

        @Override
        public void run()
        {
            try
            {
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String request = "";

                while ((request = in.readLine()) != null)
                {
                    if(request.startsWith(Requests.QUIT))
                    {
                        quitCMD(request);
                    }
                    else if(request.startsWith(Requests.LOGIN))
                    {
                        loginCMD(request);
                    }
                    else if(request.startsWith(Requests.SIGNUP))
                    {
                        signupCMD(request);
                    }
                    else if(request.startsWith(Requests.CHAT))
                    {
                        sendCMD(request);
                    }
                    else if(request.startsWith(Requests.LOAD_MESSAGES))
                    {
                        loadMessagesCMD(request);
                    }
                    else if(request.startsWith(Requests.LOAD_CHATS))
                    {
                        loadConversationsCMD(request);
                    }
                    else
                    {
                        out.println("your request is invalid");
                    }
                }
            }
            catch (IOException e)
            {
                shutDown();
            }
        }
        public void sendMessage(String message)
        {
            out.println(message);
        }
        public void shutDown()
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
            catch (IOException e){/*ignore*/}
        }
        public void shutDown(String key)
        {
            try
            {
                in.close();
                out.close();
                connections.remove(key);
                if (!client.isClosed())
                {

                    client.close();
                }
            }
            catch (IOException e){/*ignore*/}
        }
    }

    public static void main(String[] args)
    {
        new Server().run();
    }
}
