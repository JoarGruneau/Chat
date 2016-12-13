package Chat;

import java.util.Observable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Conversation extends Observable {
    private StringBuilder conversation;
    
    public Conversation() {
        conversation = new StringBuilder();
    }
    public String getHTML() {
        return conversation.toString();
    }
    public void addMessage(String message, String name, String color) {
        conversation.append("<p><font color=").append(color).append(">")
                .append("<b>").append(name).append(": ").append("</b>")
                .append(message).append("</font>").append("</p>");
        notifyObservers();
    }
    @Override
    public void notifyObservers(){
        setChanged();
        super.notifyObservers();
    }
    public void addInfo(String info) {
        conversation.append("<p>")
                .append("<b>").append(info).append("</b>").append("</p>");
        notifyObservers();
    }
}
