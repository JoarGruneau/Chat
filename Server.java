
package Chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Connection{
    ChatWindow chatWindow;
    
    public Server(int port, boolean multiConversation) throws IOException {
        
        this.multiConversation = multiConversation;
        ServerSocket listener = new ServerSocket(port);
        Socket socket = listener.accept();
        socket.setKeepAlive(true);
        if(multiConversation) {
            chatWindow = new ChatWindow(this);
        }
        else {
            chatWindow = new ChatWindow(this, socket);
        }
        chatWindow.setTitle("Server");
        sockets.add(socket);
        Thread messageParser = new Thread(new MessageParser(socket, 
                chatWindow.getConversation(), chatWindow.getController(), 
                this));
        
        messageParser.start();
        
        Runnable acceptOthers = () -> {
            while (true) {
                try {
                    Socket newSocket = listener.accept();
                    sockets.add(newSocket);
                    Thread newMessageParser = new Thread(new MessageParser(
                            newSocket, chatWindow.getConversation(), 
                            chatWindow.getController(), this));
                    newMessageParser.start();
                }catch (IOException ex) {
                    System.out.println("could not connecct client");
                }
            }
        };
        Thread acceptThread = new Thread(acceptOthers);
        acceptThread.start();
    }
    
    public void sendOtherClients(Socket socket, String message, String name, 
            String color) throws IOException {
        for(Socket tmpSocket: sockets) {
            if(!socket.equals(tmpSocket)) {
                super.sendMessage(tmpSocket, message, name, color, false);
            }
        }
    }
}
