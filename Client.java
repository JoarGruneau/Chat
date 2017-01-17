
package Chat;

import java.io.IOException;
import java.net.Socket;

/**
 *The client Chat side
 * @author joar
 */
public class Client extends Connection{

    /**
     *
     * @param ip String of the ip to the server
     * @param port the port to connect to
     * @throws IOException if unable to connect
     */
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
