package InterFace.CommandLine;
import DataBase.MySqlConnection;
import java.util.Scanner;


public class LoginRegister
{
    public static void Run(){

        int commend = 0;

        while (commend != 3) {

            PrintChoices();
            commend = new Scanner(System.in).nextInt();
            if(commend == 3) return;
            cmd(commend);
        }
    }


    private static void PrintChoices(){

        System.out.println("1- Sign-up");
        System.out.println("2- Login");
        System.out.println("3- Quit");
        System.out.print("Enter : ");
    }

    private static  void cmd(int commend){

        switch (commend) {

            case 1: Signup();  break;
            case 2: Login();   break;
        }
    }


    private static void Signup(){

        System.out.print("Enter Number : ");
        String number = new Scanner(System.in).nextLine();

        System.out.print("Enter Password : ");
        String password = new Scanner(System.in).nextLine();

        String response = MySqlConnection.Signup(number,password);
        System.out.println( response + "\n");
    }


    private static void Login(){

        System.out.print("Enter Number : ");
        String number = new Scanner(System.in).nextLine();

        System.out.print("Enter Password : ");
        String password = new Scanner(System.in).nextLine();

        String response = MySqlConnection.Login(number,password);
        System.out.println( response + "\n");
    }


}
