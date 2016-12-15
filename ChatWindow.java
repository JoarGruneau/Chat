
package Chat;

import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JFrame;

public class ChatWindow extends JFrame {
    private Conversation conversation;
    private View view;
    private Controller controller;
    
    public ChatWindow(Connection connection, Socket socket) {
        this.conversation = new Conversation();
        this.view = new View(conversation);
        this.controller = new Controller(view, conversation, connection, 
                socket);
        
        ChatWindow.this.add(controller);
        ChatWindow.this.pack();
        ChatWindow.this.setVisible(true);
        ChatWindow.this.setResizable(false);
        ChatWindow.this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public ChatWindow(Connection connection) {
        this.conversation = new Conversation();
        this.view = new View(conversation);
        this.controller = new Controller(view, conversation, connection);
        
        ChatWindow.this.add(controller);
        ChatWindow.this.pack();
        ChatWindow.this.setVisible(true);
        ChatWindow.this.setResizable(false);
        ChatWindow.this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public Conversation getConversation() {
        return conversation;
    }
    
    public Controller getController() {
        return controller;
    }
}
