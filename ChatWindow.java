
package Chat;

import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class ChatWindow extends JFrame {
    private Conversation conversation;
    private View view;
    private Controller controller;
    
    public ChatWindow(Connection connection, Socket socket, boolean accepted) {
        this.conversation = new Conversation();
        this.view = new View(conversation);
        this.controller = new Controller(view, conversation, connection, 
                accepted, socket);
        
        ChatWindow.this.add(controller);
        ChatWindow.this.pack();
        ChatWindow.this.setVisible(true);
        ChatWindow.this.setResizable(false);
        ChatWindow.this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try{
                    connection.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(ChatWindow.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public ChatWindow(Connection connection, boolean accepted) {
        this.conversation = new Conversation();
        this.view = new View(conversation);
        this.controller = new Controller(view, conversation, 
                connection, accepted);
        
        ChatWindow.this.add(controller);
        ChatWindow.this.pack();
        ChatWindow.this.setVisible(true);
        ChatWindow.this.setResizable(false);
        ChatWindow.this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try{
                    connection.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(ChatWindow.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public Conversation getConversation() {
        return conversation;
    }
    
    public Controller getController() {
        return controller;
    }
}
