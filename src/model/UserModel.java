package model;

public class UserModel
{
    private int userID;
    private String userNumber;
    private String password;
    private String verifyCode;

    public UserModel(int userID, String userNumber, String password, String verifyCode)
    {
        this.userID = userID;
        this.userNumber = userNumber;
        this.password = password;
        this.verifyCode = verifyCode;
    }


    public UserModel() {
    }

    public int userID() {
        return userID;
    }
    public void userID(int userID) {this.userID = userID;}

    public String userNumber() {
        return userNumber;
    }
    public void userNumber(String userName) {this.userNumber = userName;}

    public String password() {
        return password;
    }
    public void password(String password) {
        this.password = password;
    }

    public String verifyCode() {
        return verifyCode;
    }
    public void verifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
