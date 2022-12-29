package dataBase;

import model.MessageModel;
import java.sql.*;
import java.util.ArrayList;

public class MySqlConnection
{
    public static Connection Connect()
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


    public static String IsAvailable(String number)
    {
        Statement stmt = null;
        Connection con = Connect();
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

    public static String IsRegister(String number)
    {
        Statement stmt = null;
        Connection con = Connect();
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
        Connection con = Connect();
        String id="-1";

        if (con != null)
        {
            try
            {
                String availability = IsAvailable(number);

                if(!availability.equals("available"))  return availability;

                statement = con.createStatement();
                int res = statement.executeUpdate("INSERT INTO user (number,password) VALUES ( '"+number+"','"+password+"')",Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = statement.getGeneratedKeys();

                if (rs.next())
                {
                    id = rs.getInt(1)+" : " + number;
                }
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
        Connection con = Connect();

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
                        return "Successfully logged in" + " :  " + user.getString("number");
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
        Connection con = Connect();
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

    public static String sendMessage(MessageModel modleMessage)
    {
        Statement statement = null;
        Connection con = Connect();
        if (con != null)
        {
            try
            {
                statement = con.createStatement();
                String from = modleMessage.fromNumber();
                String text = modleMessage.message();
                String sentDataTime = modleMessage.sentDataTime();
                String name = modleMessage.chatName();
                int userID = -1;
                int conversationId = -1;

                String res = chickChat(name);
                if(res.equals("available"))
                {
                    statement.executeUpdate("INSERT INTO conversation (name) VALUES ( '" + name + "')");

                    ResultSet conversationResultSet = statement.executeQuery("SELECT * FROM `conversation` WHERE name=\""+name+"\" ");
                    if(conversationResultSet.next())
                    {
                        conversationId =   conversationResultSet.getInt("conversation_id");
                        statement.executeUpdate("INSERT INTO message (from_number,text,sent_datetime,conversation_id) VALUES ( '"+from+"','"+ text+"','"+ sentDataTime +"','"+ conversationId +"')");
                    }


                    ResultSet userIDResultSet = statement.executeQuery("SELECT * FROM `user` WHERE number=\""+from+"\" ");
                    if(userIDResultSet.next())
                    {
                        userID =   userIDResultSet.getInt("user_id");
                        statement.executeUpdate("INSERT INTO groub_member (user_id,conversation_id) VALUES ( '" + userID + "','"+conversationId +"')");
                    }
                }
                else
                {
                    ResultSet conversationResultSet = statement.executeQuery("SELECT * FROM `conversation` WHERE name=\""+res+"\" ");
                    if(conversationResultSet.next())
                    {
                        conversationId =   conversationResultSet.getInt("conversation_id");
                        statement.executeUpdate("INSERT INTO message (from_number,text,sent_datetime,conversation_id) VALUES ( '"+from+"','"+ text+"','"+ sentDataTime +"','"+ conversationId +"')");
                    }
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



    public  static ArrayList<String> GetMessages(String chatName)
    {
        Statement statement = null;
        Connection con = Connect();
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
}
