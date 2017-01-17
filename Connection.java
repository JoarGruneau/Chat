package Chat;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *Abstract class for client/server connection methods
 * @author joar
 */
public abstract class Connection {

    /**
     *Hash map with stored cryptos for each socket
     */
    protected HashMap<Socket, Crypto> cryptos = new HashMap();

    /**
     *List of sockets
     */
    protected ArrayList<Socket> sockets = new ArrayList();

    /**
     *Hash map with stored names for each socket
     */
    protected HashMap<Socket, String> names = new HashMap();

    /**
     *true == multi conversation
     */
    protected boolean multiConversation = false;
    private static int port = 1025;
    
    /**
     *Gets a new port
     * @return port as string
     */
    public String getPort() {
        port++;
        if(port > 65534) {
            port = 1025;
        }
        return Integer.toString(port);
    }
    
    /**
     *Sends a message to all sockets
     * @param message  message to be sent
     * @param name name of the sender
     * @param color color of font
     * @param sendCryptoStart in encryption start should be sent
     * @throws Exception
     */
    public void sendMessage(String message, String name, 
            String color, boolean sendCryptoStart) throws Exception {
        
        for(Socket socket: sockets) {
            sendMessage(socket, message, name, color, sendCryptoStart);
        }
    }
    
    /**
     *Sends a message to every socket except a specified socket
     * @param socket Socket not to be sent to
     * @param message message to be sent
     * @param name name of the sender
     * @param color color of font
     * @throws Exception
     */
    public void sendOtherClients(Socket socket, String message, String name, 
            String color) throws Exception {
        for(Socket tmpSocket: sockets) {
            if(!socket.equals(tmpSocket)) {
                sendMessage(tmpSocket, message, name, color, false);
            }
        }
    }
    
    /**
     *Sends a message to every socket
     * @param socket The Socket to send to
     * @param message message to be sent
     * @param name name of the sender
     * @param color color of font
     * @param sendCryptoStart in encryption start should be sent
     * @throws Exception
     */
    public void sendMessage(Socket socket, String message, String name, 
            String color, boolean sendCryptoStart) throws Exception {
        if(!sockets.contains(socket)) {
            return;
        }
        message = StringEscapeUtils.escapeHtml3(message);
        StringBuilder outgoing = new StringBuilder();
        StringBuilder innerMessage = new StringBuilder();
        
        if( !"".equals(name)){
            outgoing.append(Constants.MESSAGE_NAME).append(name).append(">");
        }
        
        else{
            outgoing.append(Constants.MESSAGE_START);
        }
        
        if(!"".equals(color)){
            innerMessage.append(Constants.TEXT_COLOR).append(color).append(">");
        }
        
        else{
            innerMessage.append(Constants.TEXT_START);
        }
        
        innerMessage.append(message).append(Constants.TEXT_STOP);
        
        if(hasCrypto(socket)) {
            Crypto crypto = getCrypto(socket);
            String encoded;
            
            if(sendCryptoStart) {
                outgoing.append(Constants.ENCRYPTED_TYPE).
                        append(crypto.getType()).append(" key=")
                        .append(crypto.getKey()).append(">");
            }
            
            else{
                outgoing.append(Constants.ENCRYPTED_TYPE)
                        .append(crypto.getType()).append(">");
            }
            
            try {
                encoded = crypto.encodeMessage(innerMessage.toString());
                outgoing.append(encoded);
                outgoing.append(Constants.ENCRYPTED_STOP);
            } 
            
            catch (Exception ex) {
                deleteCrypto(socket);
                outgoing.append(innerMessage.toString());
            }
        }
        
        else{
            outgoing.append(innerMessage.toString());
        }
        
        outgoing.append(Constants.MESSAGE_STOP);
        send(socket, outgoing.toString());
    }
    
    /**
     *Returns true if a Socket has a crypto
     * @param socket Socket
     * @return boolean
     */
    public boolean hasCrypto(Socket socket) {
        return cryptos.containsKey(socket);
    }
    
    /**
     * Gets the crypto for the Socket
     * @param socket Socket
     * @return The specified Crypto
     */
    public Crypto getCrypto(Socket socket) {
        return cryptos.get(socket);
    }
    
    /**
     *Sets a Crypto
     * @param socket Socket to set Crypto for
     * @param type Type of crypto
     * @throws Exception raised if unable to set crypto
     */
    public void setCrypto(Socket socket, String type) throws Exception {
        cryptos.put(socket, new Crypto(type));
    }
    
    /**
     *Removes the crypto to corresponding socket
     * @param socket the Socket
     */
    public void deleteCrypto(Socket socket) {
        cryptos.remove(socket);
    }
    
    /**
     *Set name for a socket
     * @param socket the Socket
     * @param name the name
     */
    public void setName(Socket socket, String name) {
        names.put(socket, name);
    }
    
    /**
     * Get name for corresponding socket
     * @param socket the Socket
     * @return the name
     */
    public String getName(Socket socket) {
        if(names.containsKey(socket)) {
            return names.get(socket);
        }
        return socket.getInetAddress().toString();
    }
    
    /**
     *Sends a file request to specified Socket
     * @param socket the Socket
     * @param message explanatory message
     * @param name senders name
     * @param fileName the file name
     * @param size the file size
     * @throws Exception
     */
    public void sendFileTransferRequest(Socket socket, String message,  
            String name, String fileName, String size) throws Exception {
        message = StringEscapeUtils.escapeHtml3(message);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constants.FILE_REQUEST_NAME).append(fileName)
                .append(" size=").append(size).append(">")
                .append(message).append(Constants.FILE_REQUEST_STOP);
        sendEncrypted(socket, stringBuilder.toString(), name);
    }
    
    /**
     *Reply to an file request to a specified Socket
     * @param socket the Socket
     * @param name name of sender
     * @param answer the reply = yes/no
     * @param reason reason for answer
     * @param port port to send to, otherwise ""
     * @param type type of encryption if desired, otherwise "" 
     * @param key key to encryption, otherwise ""
     * @throws Exception
     */
    public void replyFileTransfer(Socket socket, String name, String answer, 
            String reason, String port, String type, String key) 
            throws Exception {
        reason = StringEscapeUtils.escapeHtml3(reason);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constants.FILE_RESPONES).append(answer)
                .append(" port=").append(port).append(" type=").append(type)
                .append(" key=").append(key).append(">")
                .append(reason).append(Constants.FILE_RESPONSE_STOP);
        sendEncrypted(socket, stringBuilder.toString(), name);
    }
    
    /**
     *Sends a key request to specified socket
     * @param socket the Socket
     * @param message explanatory message
     * @param name the senders name
     * @param type typo of key desired
     * @throws Exception
     */
    public void sendKeyRequest(Socket socket, String message, String name, 
            String type) throws Exception {
        message = StringEscapeUtils.escapeHtml3(message);
 
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constants.KEY_REQUEST).append(type).append(">")
                .append(message).append(Constants.KEY_REQUEST_STOP);
        sendEncrypted(socket, stringBuilder.toString(), name);

    }
    
    /**
     *Sends a key to specified Socket
     * @param socket the Socket
     * @param name senders name
     * @throws Exception
     */
    public void sendKey(Socket socket, String name) 
            throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constants.MESSAGE_NAME).append(name).append(">");
        stringBuilder.append(Constants.KEY_RESPONSE)
                .append(getCrypto(socket).getType()).append(" key=")
                .append(getCrypto(socket).getKey()).append(">")
                .append(Constants.KEY_RESPONSE_STOP);
        stringBuilder.append(Constants.MESSAGE_STOP);
        send(socket, stringBuilder.toString());

    }
    
    /**
     *Sends an answer to a join request to a conversation to specified Socket
     * @param socket the socket
     * @param name name of sender
     * @param ans the answer = yes/no
     * @throws Exception
     */
    public void sendJoinReply(Socket socket, String name, String ans) 
            throws Exception {
        String outgoing = Constants.REQUEST_ANS + ans +">" 
                 + Constants.REQUEST_STOP;
        sendEncrypted(socket, outgoing, name);
    }
    
    /**
     *Sends a join request to a conversation to a specified Socket
     * @param message explanatory message for request
     * @param name name of sender
     * @throws Exception
     */
    public void sendJoinRequest(String message, String name) 
            throws Exception {
        for(Socket socket: sockets) {
            String outgoing = Constants.REQUEST + message + 
                    Constants.REQUEST_STOP;
            sendEncrypted(socket, outgoing, name);
        }
    }
    
    /**
     *Disconnect from conversation
     * @throws Exception
     */
    public void disconnect() throws Exception {
        for(Socket socket: sockets) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Constants.MESSAGE_START);
            stringBuilder.append(Constants.DISCONNECT)
                    .append(Constants.MESSAGE_STOP);
            send(socket, stringBuilder.toString());
        }
    }
    
    private void sendEncrypted(Socket socket, String message, String name) 
            throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constants.MESSAGE_NAME).append(name).append(">");
        if(hasCrypto(socket)) {
            Crypto crypto = getCrypto(socket);
            stringBuilder.append(Constants.ENCRYPTED_TYPE)
                    .append(crypto.getType()).append(">")
                    .append(crypto.encodeMessage(message))
                    .append(Constants.ENCRYPTED_STOP);
        }
        else {
            stringBuilder.append(message);
        }
        
        stringBuilder.append(Constants.MESSAGE_STOP);
        OutputStreamWriter output =new OutputStreamWriter(
                                    socket.getOutputStream(), "UTF-8");
        output.write(stringBuilder.toString(), 0, 
                stringBuilder.toString().length());
        output.flush();
    }
    
        private void send(Socket socket, String message) 
            throws Exception {
        OutputStreamWriter output =new OutputStreamWriter(
                                    socket.getOutputStream(), "UTF-8");
        output.write(message, 0, message.length());
        output.flush();
    }

    

}
