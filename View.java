
package Chat;

import java.awt.Dimension;
import java.util.Observable;
import javax.swing.JEditorPane;

/**
 *View of the conversation
 * @author joar
 */
public class View extends JEditorPane{
    Conversation conversation;
    
    /**
     *Creates html view of a given conversation
     * @param conversation the conversation
     */
    public View(Conversation conversation) {
        this.conversation = conversation;
        this.conversation.addObserver((Observable o, Object arg) -> {
            View.this.setText(((Conversation) o).getHTML());
        });
        View.this.setContentType("text/html");
        View.this.setEditable(false);
    }
    
}