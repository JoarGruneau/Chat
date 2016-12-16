
package Chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Connection{
    ChatWindow chatWindow;
    
    public Server(int port, boolean multiConversation) throws IOException {
        
        this.multiConversation = multiConversation;
        ServerSocket listener = new ServerSocket(port);
        
        Runnable acceptOthers = () -> {
            try {
                Socket socket = listener.accept();
                if(multiConversation) {
                    chatWindow = new ChatWindow(this, true);
                }
                else {
                    chatWindow = new ChatWindow(this, socket, true);
                }
                chatWindow.setTitle("Server");
                //sockets.add(socket);
                Thread messageParser = new Thread(new MessageParser(socket, 
                        chatWindow.getConversation(), chatWindow.getController(), 
                        this, false));
                messageParser.start();
                while (true) {
                    Socket newSocket = listener.accept();
                    //sockets.add(newSocket);
                    if(multiConversation) {
                        Thread newMessageParser = new Thread(new MessageParser(
                                newSocket, chatWindow.getConversation(), 
                                chatWindow.getController(), this, false));
                        newMessageParser.start();
                    }
                    else {
                        ChatWindow newChat = new ChatWindow(this, 
                                newSocket, true);
                        Thread newMessageParser = new Thread(new MessageParser(
                                newSocket, newChat.getConversation(), 
                                newChat.getController(), this, false));
                        newChat.setTitle("Server");
                        newMessageParser.start();
                    }
                }
            }
            catch (Exception e) {
                chatWindow.getConversation()
                        .addInfo("Error while creating server");
            }
        };
        Thread acceptThread = new Thread(acceptOthers);
        acceptThread.start();
    }
    
}
