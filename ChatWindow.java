
package Chat;

import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *The chat window
 * @author joar
 */
public class ChatWindow extends JFrame {
    private Conversation conversation;
    private View view;
    private Controller controller;
    
    /**
     *Constructor for single conversation
     * @param connection the instance of the TCP connection class
     * @param socket The socket for the single conversation
     * @param accepted boolean, client accepted to conversation
     */
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
                    connection.disconnect(controller.name());
                } catch (Exception ex) {
                    Logger.getLogger(ChatWindow.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    /**
     *Constructor for multi conversation
     * @param connection the instance of the TCP connection class
     * @param accepted boolean, client accepted to conversation
     */
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
                    connection.disconnect(controller.name());
                } catch (Exception ex) {
                    Logger.getLogger(ChatWindow.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    /**
     *Get the conversation
     * @return instance on Conversation
     */
    public Conversation getConversation() {
        return conversation;
    }
    
    /**
     *Get controller
     * @return the controller
     */
    public Controller getController() {
        return controller;
    }
}
