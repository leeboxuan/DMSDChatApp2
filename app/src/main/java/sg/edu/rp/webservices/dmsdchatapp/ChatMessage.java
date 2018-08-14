package sg.edu.rp.webservices.dmsdchatapp;

import android.text.format.DateFormat;

public class ChatMessage {

    private String messageText;
    private String messageUser;
    private long messageTime;
    private String id;

    public ChatMessage() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public ChatMessage(String messageText, long messageTime, String messageUser) {
        this.messageText = messageText;
        this.messageTime = messageTime;
        this.messageUser = messageUser;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageUser() {
        return messageUser;
    }


    public long getMessageTime() {

        return messageTime;
    }

}