
package Chat;

import java.awt.Dimension;
import java.util.Observable;
import javax.swing.JEditorPane;


public class View extends JEditorPane{
    Conversation conversation;
    
    public View(Conversation conversation) {
        this.conversation = conversation;
        this.conversation.addObserver((Observable o, Object arg) -> {
            View.this.setText(((Conversation) o).getHTML());
        });
        View.this.setContentType("text/html");
        View.this.setEditable(false);
    }
    
}