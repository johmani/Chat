package userInterFace.CommandLine;

import java.util.Scanner;

public class MainCMD
{
    public static void main(String[] args)
    {
         Run();
    }


    static void Run()
    {
        String cmd;

        System.out.print(">");
        while ((cmd = new Scanner(System.in).nextLine()) != null)
        {

            if(cmd.equals("login"))
            {
                LoginRegister.Login();
            }
            else if(cmd.equals("signup"))
            {
                LoginRegister.Signup();
            }
            else
            {
                System.out.println("wrong cmd");
            }
            System.out.print(">");
        }
    }
}
