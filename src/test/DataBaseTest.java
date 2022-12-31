package test;

import dataBase.MySqlConnection;

import java.util.ArrayList;

public class DataBaseTest {
    public static void main(String[] args)
    {

//        MessageModel newMessage = new MessageModel("mohamd","hi","1:30","mohamd,momo");
//        MySqlConnection.sendMessage(newMessage);

        ArrayList<String> ids = MySqlConnection.getConversations("mohamd");
        for(String id : ids){
            System.out.println(id);
        }


    }
}
