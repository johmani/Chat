package DataBase;

import java.sql.*;

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
                stmt = (Statement) con.createStatement();
                ResultSet user = stmt.executeQuery("SELECT * FROM `user` WHERE number=\""+number+"\" ");

                if(user.next()) return "user name already token";
                else return "available";

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
                    id = rs.getInt(1)+" : "+number;
                }
            }
            catch (SQLException throwable)
            {
                throwable.printStackTrace();
            }

            return id;
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
                        return user.getInt("user_id") + ":  " + user.getString("number");
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
}
