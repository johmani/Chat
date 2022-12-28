package userInterFace.GUI;

import dataBase.MySqlConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterMenu extends JFrame implements ActionListener {

    Container container = getContentPane();
    JLabel numberLabel = new JLabel("USERNAME");
    JLabel passwordLabel = new JLabel("PASSWORD");
    JTextField numberTextField = new JTextField();
    JPasswordField passwordTextField = new JPasswordField();
    JButton loginButton = new JButton("LOGIN");
    JCheckBox showPassword = new JCheckBox("Show Password");
    JLabel not_a_user = new JLabel("not a user?");
    JButton signupButton = new JButton("Signup");

    RegisterMenu()
    {
        setLayoutManager();
        setLocationAndSize();
        addComponentsToContainer();
        addActionEvent();
    }

    public void setLayoutManager()
    {
        container.setLayout(null);
    }

    public void setLocationAndSize()
    {
        numberLabel.setBounds(50, 150, 100, 30);
        passwordLabel.setBounds(50, 220, 100, 30);
        numberTextField.setBounds(150, 150, 150, 30);
        passwordTextField.setBounds(150, 220, 150, 30);
        showPassword.setBounds(150, 250, 150, 30);
        loginButton.setBounds(50, 300, 100, 30);
        not_a_user.setBounds(50, 400, 100, 30);
        signupButton.setBounds(120, 400, 100, 30);
    }

    public void addComponentsToContainer()
    {
        container.add(numberLabel);
        container.add(passwordLabel);
        container.add(numberTextField);
        container.add(passwordTextField);
        container.add(showPassword);
        container.add(loginButton);
        container.add(not_a_user);
        container.add(signupButton);
    }

    public void addActionEvent()
    {
        loginButton.addActionListener(this);
        showPassword.addActionListener(this);
        signupButton.addActionListener(this);
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        MySqlConnection sql = new MySqlConnection();

        if (e.getSource() == loginButton)
        {
            String res = sql.Login(numberTextField.getText(),passwordTextField.getText());
            System.out.println(res);
            JOptionPane.showMessageDialog(this, res);
        }

        if (e.getSource() == showPassword)
        {
            if (showPassword.isSelected()) passwordTextField.setEchoChar((char) 0);
            else  passwordTextField.setEchoChar('*');
        }

        if (e.getSource() == signupButton)
        {
            this.setVisible(true);
        }
    }
}
