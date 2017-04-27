package Chat;

import java.util.Observable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The conversation for the Chat program
 *
 * @author joar
 */
public class Conversation extends Observable {

    private StringBuilder conversation;

    /**
     * Creates a new conversation
     */
    public Conversation() {
        conversation = new StringBuilder();
    }

    /**
     * gets the HTML code of the conversation
     *
     * @return
     */
    public String getHTML() {
        return conversation.toString();
    }

    /**
     * Adds a new message to the conversation
     *
     * @param message the message
     * @param name name of sender
     * @param color color of font
     */
    public synchronized void addMessage(String message, String name, String color) {
        conversation.append("<p><font color=").append(color).append(">")
                .append("<b>").append(name).append(": ").append("</b>")
                .append(message).append("</font>").append("</p>");
        notifyObservers();
    }

    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }

    /**
     * Adds som information to the conversation
     *
     * @param info the info being added
     */
    public void addInfo(String info) {
        conversation.append("<p>")
                .append("<b>").append(info).append("</b>").append("</p>");
        notifyObservers();
    }
}
