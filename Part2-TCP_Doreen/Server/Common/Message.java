package Server.Common;

import java.io.Serializable;

public class Message implements Serializable {
    String messageText;

    public Message(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
