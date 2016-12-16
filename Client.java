
package Chat;

import java.io.IOException;
import java.net.Socket;

public class Client extends Connection{
    public Client(String ip, int port) throws IOException {
        Socket socket = new Socket(ip, port);
        sockets.add(socket);
        ChatWindow chatWindow = new ChatWindow(this, false);
        chatWindow.setTitle("Client");
        Thread messageParser = new Thread(new MessageParser(socket, 
                chatWindow.getConversation(), 
                chatWindow.getController(), this, true));
        messageParser.start();
    }
}
