package serverClient;

import dataBase.MySqlConnection;
import model.MessageModel;
import model.UserModel;
import javax.crypto.SecretKey;
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

    private SecretKey symmetricKey;

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

                ConnectionHandler handler = new ConnectionHandler(client);

                int n = new Random().nextInt(1000000);
                handler.user.userNumber(Integer.toString(n));

                connections.put(handler.user.userNumber(),handler);
                pool.execute(handler);

                System.out.println("New client connected " + handler.user.userNumber() + ",Host :"+  client.getInetAddress().getHostAddress());
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
        private boolean loggedin;
        private SecretKey symmetricKey;


        public ConnectionHandler(Socket client)
        {
            this.client = client;
            user = new UserModel();
        }

        private void quitCMD(String request)
        {
            System.out.println(user.userNumber() + " has quit");
            shutDown(user.userNumber());
        }

        private String loginCMD(String request) throws Exception
        {
            String[] req = request.split("-");
            if(req.length == 3)
            {
                String response = MySqlConnection.Login(req[1],req[2]);

                if(response.startsWith("Successfully"))
                {
                    loggedin = true;
                    ConnectionHandler connection = connections.remove(user.userNumber());

                    user.userNumber(req[1]);
                    user.password(req[2]);

                    connections.put(user.userNumber(),connection);
                    symmetricKey = Symmetric.createAESKey(user.password());
                }
                return  response;
            }
            else
            {
                return "invalid request";
            }
        }

        private String signupCMD(String request)
        {
            String[] req = request.split("-");
            if(req.length == 3)
            {
                String number = req[1];
                String password = req[2];
                String response = MySqlConnection.Signup(number,password);
                return  response;
            }
            else
            {
                return "invalid request";
            }
        }

        private String sendCMD(String request) throws Exception
        {
            if(loggedin)
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
                        String toPassword = MySqlConnection.userNumber2password(to);
                        SecretKey toSymmetricKey = Symmetric.createAESKey(toPassword);

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
                        LocalDateTime now = LocalDateTime.now();
                        String chatName = from +","+ to;

                        MessageModel newMessage = new MessageModel(from,message,dtf.format(now),chatName);
                        MySqlConnection.sendMessage(newMessage);

                        if(connections.get(to)!=null)
                        {
                            broadCast(to, Symmetric.encrypt( from + " : " + message, toSymmetricKey));
                            return "+"+ from + " : " + message;
                        }
                        else
                        {
                            return  "-"+ from + " : " + message;
                        }

                    }
                    else
                    {
                        return response;
                    }
                }
                else
                {
                    return "invalid request";
                }
            }
            else
            {
                return "you must login first , use /login-number-password to login";
            }
        }

        private ArrayList<String> loadMessagesCMD(String request)
        {
           if(loggedin)
           {
               String[] req = request.split("-");
               if(req.length == 2)
               {
                   String to = req[1];
                   String chat = user.userNumber()+","+to;
                   ArrayList<String> messages = MySqlConnection.getMessages(chat);
                   return messages;
               }
               else
               {
                   ArrayList<String> res = new ArrayList<>();
                   res.add("your request is invalid");
                   return res;
               }
           }
           else
           {
               ArrayList<String> res = new ArrayList<>();
               res.add("You must login first , use /login-number-password to login");
               return res;
           }
        }

        private ArrayList<String> loadConversationsCMD(String request)
        {
            if(loggedin)
            {
                String[] req = request.split("-");
                if(req.length == 1)
                {
                    ArrayList<String> conversations = MySqlConnection.getConversations(user.userNumber());
                    return conversations;
                }
                else
                {
                    ArrayList<String> res = new ArrayList<>();
                    res.add("your request is invalid");
                    return res;
                }
            }
            else
            {
                ArrayList<String> res = new ArrayList<>();
                res.add("You must login first , use /login-number-password to login");
                return res;
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
                    //System.out.print("request : " + request + " --> ");

                    if(request.startsWith(Requests.QUIT))
                    {
                        quitCMD(request);
                        break;
                    }
                    else if(request.startsWith(Requests.LOGIN))
                    {
                        String response = loginCMD(request);
                        out.println(response);
                    }
                    else if(request.startsWith(Requests.SIGNUP))
                    {
                        String response =  signupCMD(request);
                        out.println(response);
                    }
                    else
                    {
                        String requestDecrypt = Symmetric.decrypt(request,symmetricKey);
                        System.out.println("request : " + request + " --> " + requestDecrypt);

                        if(requestDecrypt.startsWith(Requests.SEND))
                        {
                            String response =  sendCMD(requestDecrypt);
                            String mac = Symmetric.MAC(response,symmetricKey);
                            out.println(Symmetric.encrypt(response,symmetricKey) + "mac" + mac);
                        }
                        else if(requestDecrypt.startsWith(Requests.LOAD_MESSAGES))
                        {
                            ArrayList<String> response = loadMessagesCMD(requestDecrypt);
                            for (String message: response)
                            {
                                String mac = Symmetric.MAC(message,symmetricKey);
                                out.println(Symmetric.encrypt(message,symmetricKey) + "mac" + mac);
                            }
                        }
                        else if(requestDecrypt.startsWith(Requests.LOAD_CHATS))
                        {
                            ArrayList<String> response = loadConversationsCMD(requestDecrypt);
                            for (String message: response)
                            {
                                String mac = Symmetric.MAC(message,symmetricKey);
                                out.println(Symmetric.encrypt(message,symmetricKey) + "mac" + mac);
                            }
                        }
                        else
                        {
                            String response = "your request is invalid";
                            String mac = Symmetric.MAC(response,symmetricKey);
                            out.println(Symmetric.encrypt(response,symmetricKey) + "mac" + mac);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
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
            catch (IOException e){/* ignore */}
        }
    }

    public static void main(String[] args)
    {
        new Server().run();
    }
}
