package model;

public class MessageModel
{
    private String fromNumber;
    private String message;
    private String sentDataTime;
    private String chatName;

    public MessageModel() {}

    public MessageModel(String fromNumber, String message, String sentDataTime, String chatName) {
        this.fromNumber = fromNumber;
        this.message = message;
        this.sentDataTime = sentDataTime;
        this.chatName = chatName;
    }

    public String fromNumber() {
        return fromNumber;
    }
    public void fromNumber(String fromNumber) {
        this.fromNumber = fromNumber;
    }

    public String message() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String sentDataTime() {
        return sentDataTime;
    }
    public void sentDataTime(String sentDataTime) {
        this.sentDataTime = sentDataTime;
    }

    public String chatName() {
        return chatName;
    }
    public void chatName(String conversationId) {
        this.chatName = conversationId;
    }
}
