
package Chat;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client extends Connection{
    Socket socket;
    public Client(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        sockets.add(socket);
        ChatWindow chatWindow = new ChatWindow(this, socket);
        chatWindow.setTitle("Client");
        Thread messageParser = new Thread(new MessageParser(socket, 
                chatWindow.getConversation(), chatWindow.getController(), this));
        messageParser.start();
    }
}
