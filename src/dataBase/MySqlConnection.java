package dataBase;

import model.MessageModel;
import java.sql.*;
import java.util.ArrayList;

public class MySqlConnection
{
    public static Connection connect()
    {
        Connection con = null;
        try
        {
            con = DriverManager.getConnection("jdbc:mysql://localhost/chat", "root", "moon");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return con;
    }

    public static String isAvailable(String number)
    {
        Statement stmt = null;
        Connection con = connect();
        if (con != null)
        {
            try
            {
                stmt = con.createStatement();
                ResultSet user = stmt.executeQuery("SELECT * FROM `user` WHERE number=\""+number+"\" ");

                if(user.next()) return "user number already token";
                else return "available";

            }
            catch (SQLException throwables)
            {
                throwables.printStackTrace();
            }
        }
        return "error";
    }

    public static String isRegister(String number)
    {
        Statement stmt = null;
        Connection con = connect();
        if (con != null)
        {
            try
            {
                stmt = con.createStatement();
                ResultSet user = stmt.executeQuery("SELECT * FROM `user` WHERE number=\""+number+"\" ");

                if(user.next()) return "isRegister";
                else return number + " is not Register";

            } catch (SQLException throwables)
            {
                throwables.printStackTrace();
            }
        }
        return "error";
    }

    public static String Signup(String number ,String password)
    {
        Statement statement = null;
        Connection con = connect();
        if (con != null)
        {
            try
            {
                String availability = isAvailable(number);
                if(!availability.equals("available"))  return availability;

                statement = con.createStatement();
                statement.executeUpdate("INSERT INTO user (number,password) VALUES ( '"+number+"','"+password+"')");
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
            return "successfully registered";
        }
        return "connection error";
    }

    public static String Login(String number ,String pass)
    {
        Statement statement = null;
        Connection con = connect();

        if (con != null)
        {
            try
            {
                statement = con.createStatement();
                ResultSet user = statement.executeQuery("SELECT * FROM `user` WHERE number=\""+number+"\" ");

                if(user.next())
                {
                    if(pass.equals(user.getString("password")))
                    {
                        return "Successfully logged in" + " :" + user.getInt("user_id") + ":" + user.getString("number");
                    }
                    else
                    {
                        return "Wrong Password ";
                    }
                }
                else
                    return "user not found ";
            }
            catch (SQLException throwables)
            {
                throwables.printStackTrace();
            }
        }
        return "connection error";
    }

    public static String chickChat(String chatName)
    {
        Statement statement = null;
        Connection con = connect();
        if (con != null)
        {
            try
            {
                statement = con.createStatement();
                String[] nameSplit = chatName.split(",");
                String c1 = nameSplit[0] + "," + nameSplit[1];
                String c2 = nameSplit[1] + "," + nameSplit[0];

                if(statement.executeQuery("SELECT * FROM `conversation` WHERE name=\""+c1+"\" ").next()) return  c1;
                else if (statement.executeQuery("SELECT * FROM `conversation` WHERE name=\""+c2+"\" ").next()) return c2;
                else return "available";
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
        }
        return "chickChat error";
    }

    public static String conversationId2Name(int  conversationId)
    {
        Statement statement = null;
        Connection con = connect();
        String name = "";
        if (con != null)
        {
            try
            {
                statement = con.createStatement();
                ResultSet userIDResultSet = statement.executeQuery("SELECT * FROM `conversation` WHERE conversation_id=\""+conversationId+"\" ");
                if(userIDResultSet.next())
                {
                    name =   userIDResultSet.getString("name");
                    return name;
                }
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
        }
        return null;
    }

    public static int userNumber2Id(String number)
    {
        Statement statement = null;
        Connection con = connect();
        int user_id = -1;
        if (con != null)
        {
            try
            {
                statement = con.createStatement();
                ResultSet userIDResultSet = statement.executeQuery("SELECT * FROM `user` WHERE number=\""+number+"\" ");
                if(userIDResultSet.next())
                {
                    user_id =   userIDResultSet.getInt("user_id");
                    return user_id;
                }
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
        }
        return -1;
    }

    public static int conversationName2Id(String name)
    {
        Statement statement = null;
        Connection con = connect();
        int conversationId = -1;
        if (con != null)
        {
            try
            {
                statement = con.createStatement();
                ResultSet conversationResultSet = statement.executeQuery("SELECT * FROM `conversation` WHERE name=\""+name+"\" ");
                if(conversationResultSet.next())
                {
                    conversationId =   conversationResultSet.getInt("conversation_id");
                    return conversationId;
                }
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
        }
        return -1;
    }

    public static String sendMessage(MessageModel modleMessage)
    {
        Statement statement = null;
        Connection con = connect();
        if (con != null)
        {
            try
            {
                statement = con.createStatement();
                String from = modleMessage.fromNumber();
                String text = modleMessage.message();
                String sentDataTime = modleMessage.sentDataTime();
                String name = modleMessage.chatName();
                int user_id = -1;
                int conversation_id = -1;

                String res = chickChat(name);
                if(res.equals("available"))
                {
                    statement.executeUpdate("INSERT INTO conversation (name) VALUES ( '" + name + "')");

                    conversation_id =   conversationName2Id(name);
                    statement.executeUpdate("INSERT INTO message (from_number,text,sent_datetime,conversation_id) VALUES ( '"+from+"','"+ text+"','"+ sentDataTime +"','"+ conversation_id +"')");

                    user_id = userNumber2Id(from);
                    statement.executeUpdate("INSERT INTO groub_member (user_id,conversation_id) VALUES ( '" + user_id + "','"+conversation_id +"')");

                    String toNumber = name.split(",")[1];
                    int user_id2 = userNumber2Id(toNumber);
                    statement.executeUpdate("INSERT INTO groub_member (user_id,conversation_id) VALUES ( '" + user_id2 + "','"+conversation_id +"')");
                }
                else
                {
                    conversation_id =   conversationName2Id(res);
                    statement.executeUpdate("INSERT INTO message (from_number,text,sent_datetime,conversation_id) VALUES ( '"+from+"','"+ text+"','"+ sentDataTime +"','"+ conversation_id +"')");
                }
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
            return "send successfully";
        }
        return "connection error";
    }

    public  static ArrayList<String> getMessages(String chatName)
    {
        Statement statement = null;
        Connection con = connect();
        if (con != null)
        {
            try
            {
                statement = con.createStatement();
                int conversationId = -1;
                String text = "hi";

                String res = chickChat(chatName);
                if(!res.equals("available"))
                {
                    ResultSet conversationResultSet1 = statement.executeQuery("SELECT * FROM `conversation` WHERE name=\""+res+"\" ");
                    if(conversationResultSet1.next())
                    {
                        conversationId = conversationResultSet1.getInt("conversation_id");
                    }
                }

                ResultSet conversationResultSet2 = statement.executeQuery("SELECT * FROM `message` WHERE conversation_id=\""+conversationId+"\" ");
                ArrayList<String> messages = new ArrayList<>();
                while (conversationResultSet2.next())
                {
                    text =   conversationResultSet2.getNString("text");
                    messages.add(text);
                }
                return messages;
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
        }
        return  null;
    }

    public  static ArrayList<String> getConversations(String userNumber)
    {
        Statement statement = null;
        Connection con = connect();
        if (con != null)
        {
            try
            {
                statement = con.createStatement();

                if(isRegister(userNumber).equals("isRegister"))
                {
                    int user_id = userNumber2Id(userNumber);

                    ArrayList<String> conversation_ids = new ArrayList<>();
                    ResultSet groub_memberResultSet = statement.executeQuery("SELECT * FROM `groub_member` WHERE user_id=\""+user_id+"\" ");
                    while (groub_memberResultSet.next())
                    {
                        int conversation_id =   groub_memberResultSet.getInt("conversation_id");
                        String name = conversationId2Name(conversation_id);
                        conversation_ids.add(name);
                    }
                    return conversation_ids;
                }
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }
        }
        return  null;
    }
}
