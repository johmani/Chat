package serverClient;

import dataBase.MySqlConnection;
import model.MessageModel;
import model.UserModel;
import security.Hyper;
import security.SymmetricEncryption;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable
{
    private Hashtable<String,ConnectionHandler> connections;
    private Hashtable<String,SecretKey> keys;
    private ServerSocket server;
    private ExecutorService pool;
    private boolean done;
    private KeyPair keyPair;


    public Server()
    {
        connections = new Hashtable<>();
        keys = new Hashtable<>();
        done = false;
    }

    @Override
    public void run()
    {
        try
        {
            server = new ServerSocket(4000);
            pool = Executors.newCachedThreadPool();

            keyPair =  Hyper.generateKeyPair();
            System.out.println("The Public Key is: " + DatatypeConverter.printHexBinary(keyPair.getPublic().getEncoded()));
            System.out.println("The Private Key is: " + DatatypeConverter.printHexBinary(keyPair.getPrivate().getEncoded()));

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
        catch (IOException e) {/*ignore*/}
    }

    class  ConnectionHandler implements Runnable
    {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private UserModel user;
        private boolean loggedin = false;
        private SecretKey sessionKey;


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
                    keys.put(user.userNumber(), sessionKey);
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
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
                        LocalDateTime now = LocalDateTime.now();
                        String chatName = from +","+ to;

                        MessageModel newMessage = new MessageModel(from,message,dtf.format(now),chatName);
                        MySqlConnection.sendMessage(newMessage);

                        if(connections.get(to)!=null)
                        {
                            String messages = from + " : " + message;
                            String mac = SymmetricEncryption.MAC(messages,keys.get(to));
                            broadCast(to, SymmetricEncryption.encrypt( messages, keys.get(to)) + "mac" + mac);
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

        private SecretKey handShaking() throws Exception
        {
            PublicKey publicKey = keyPair.getPublic();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
            objectOutputStream.writeObject(publicKey);

            //Receive Session key
            String encreptSessionKey = in.readLine();
            byte[] encreptSessionKeyByte = DatatypeConverter.parseHexBinary(encreptSessionKey);

            //decrypt session key
            String decryptSessionKey = Hyper.Decrept(encreptSessionKeyByte,keyPair.getPrivate());
            encreptSessionKeyByte = DatatypeConverter.parseHexBinary(decryptSessionKey);
            SecretKey key = new SecretKeySpec(encreptSessionKeyByte, 0, encreptSessionKeyByte.length, "AES");

            return  key;
        }

        @Override
        public void run()
        {
            try
            {
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                sessionKey = handShaking();

                if(sessionKey != null)
                {
                    String response = "successful handshake";
                    String mac = SymmetricEncryption.MAC(response,sessionKey);
                    out.println(SymmetricEncryption.encrypt(response,sessionKey) + "mac" + mac);
                }
                else
                {
                    String response = "wrong handshake";
                    String mac = SymmetricEncryption.MAC(response,sessionKey);
                    out.println(SymmetricEncryption.encrypt(response,sessionKey) + "mac" + mac);
                }
                System.out.println("The Session key  is: " + DatatypeConverter.printHexBinary(sessionKey.getEncoded()));


                String request = "";
                while ((request = in.readLine()) != null)
                {
                    String requestDecrypt = SymmetricEncryption.decrypt(request,sessionKey);

                    System.out.println("request : " + request + " --> " + requestDecrypt);

                    if(requestDecrypt.startsWith(Requests.QUIT))
                    {
                        quitCMD(requestDecrypt);
                        break;
                    }
                    else if(requestDecrypt.startsWith(Requests.LOGIN))
                    {
                        String response = loginCMD(requestDecrypt);
                        String mac = SymmetricEncryption.MAC(response,sessionKey);
                        out.println(SymmetricEncryption.encrypt(response,sessionKey) + "mac" + mac);
                    }
                    else if(requestDecrypt.startsWith(Requests.SIGNUP))
                    {
                        String response =  signupCMD(requestDecrypt);
                        String mac = SymmetricEncryption.MAC(response,sessionKey);
                        out.println(SymmetricEncryption.encrypt(response,sessionKey) + "mac" + mac);
                    }
                    else if(requestDecrypt.startsWith(Requests.SEND))
                    {
                        String response =  sendCMD(requestDecrypt);
                        String mac = SymmetricEncryption.MAC(response,sessionKey);
                        out.println(SymmetricEncryption.encrypt(response,sessionKey) + "mac" + mac);
                    }
                    else if(requestDecrypt.startsWith(Requests.LOAD_MESSAGES))
                    {
                        ArrayList<String> response = loadMessagesCMD(requestDecrypt);
                        for (String message: response)
                        {
                            String mac = SymmetricEncryption.MAC(message,sessionKey);
                            out.println(SymmetricEncryption.encrypt(message,sessionKey) + "mac" + mac);
                        }
                    }
                    else if(requestDecrypt.startsWith(Requests.LOAD_CHATS))
                    {
                        ArrayList<String> response = loadConversationsCMD(requestDecrypt);
                        for (String message: response)
                        {
                            String mac = SymmetricEncryption.MAC(message,sessionKey);
                            out.println(SymmetricEncryption.encrypt(message,sessionKey) + "mac" + mac);
                        }
                    }
                    else
                    {
                        String response = "your request is invalid";
                        String mac = SymmetricEncryption.MAC(response,sessionKey);
                        out.println(SymmetricEncryption.encrypt(response,sessionKey) + "mac" + mac);
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
                connections.remove(key);
                in.close();
                out.close();
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
