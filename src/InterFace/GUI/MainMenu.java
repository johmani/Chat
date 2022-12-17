package InterFace.GUI;

import javax.swing.*;

public class MainMenu {
    public static void main(String[] a)
    {
        LoginMenu frame = new LoginMenu();
        frame.setTitle("Login");
        frame.setVisible(true);
        frame.setBounds(10, 10, 370, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
    }

}
