package userInterFace.CommandLine;
import dataBase.MySqlConnection;
import serverClient.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class LoginRegister
{
    public static void Run()
    {
        int commend = 0;

        while (commend != 3)
        {
            PrintChoices();
            commend = new Scanner(System.in).nextInt();
            if(commend == 3) return;
            cmd(commend);
        }
    }

    private static void PrintChoices()
    {
        System.out.println("1- Sign-up");
        System.out.println("2- Login");
        System.out.println("3- Quit");
        System.out.print("Enter : ");
    }

    private static  void cmd(int commend)
    {
        switch (commend)
        {
            case 1: Signup();  break;
            case 2: Login();   break;
        }
    }

    public static boolean Signup()
    {
        System.out.print("Number : ");
        String number = new Scanner(System.in).nextLine();

        System.out.print("Password : ");
        String password = new Scanner(System.in).nextLine();

        String response = MySqlConnection.Signup(number,password);
        System.out.println( response + "\n");

        if(response.startsWith("Successfully"))
        {
            new Client().run();
            return true;
        }
        return false;
    }

    public static boolean Login()
    {
        System.out.print("Enter Number : ");
        String number = new Scanner(System.in).nextLine();

        System.out.print("Enter Password : ");
        String password = new Scanner(System.in).nextLine();

        String response = MySqlConnection.Login(number,password);
        System.out.println( response + "\n");

        if(response.startsWith("Successfully"))
        {
            new Client().run();
            return true;
        }
        return false;
    }



    private static BufferedReader in;
    private static PrintWriter out;

    public static String Login(Socket client) throws IOException
    {

        out = new PrintWriter(client.getOutputStream(),true);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        out.print("Enter Number : ");
        String number = in.readLine();

        out.print("Enter Password : ");
        String password = in.readLine();

        String response = MySqlConnection.Login(number,password);
        out.println( response + "\n");

        return response;
    }
}
