package test;

import dataBase.MySqlConnection;

import java.util.ArrayList;

public class DataBaseTest {
    public static void main(String[] args)
    {
        ArrayList<String> messages = MySqlConnection.GetMessages("mohamd,momo");

        for(String message : messages)
        {
            System.out.println(message);
        }
    }
}
