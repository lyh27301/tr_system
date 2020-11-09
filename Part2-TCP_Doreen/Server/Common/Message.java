package Server.Common;

import java.io.Serializable;

public class Message implements Serializable {
    String messageText;
    Object remoteObject;


    public Message(String messageText) {
        this.messageText = messageText;
        this.remoteObject = null;
    }

    public String getMessageText() {
        return messageText;
    }

    public Object getMessageObject() {
        return remoteObject;
    }

    public void setMessageObject(RMItem object) {
        this.remoteObject = object;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
