package Chat;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.Timer;
import org.apache.commons.lang3.StringEscapeUtils;

public abstract class Connection {
    protected HashMap<Socket, Crypto> cryptos = new HashMap();
    protected ArrayList<Socket> sockets = new ArrayList();
    protected HashMap<Socket, String> names = new HashMap();
    protected boolean multiConversation = false;
    
    public void sendMessage(String message, String name, 
            String color, boolean sendCryptoStart) throws IOException {
        
        for(Socket socket: sockets) {
            sendMessage(socket, message, name, color, sendCryptoStart);
        }
    }
    
    public void sendOtherClients(Socket socket, String message, String name, 
            String color) throws IOException {
        for(Socket tmpSocket: sockets) {
            if(!socket.equals(tmpSocket)) {
                sendMessage(tmpSocket, message, name, color, false);
            }
        }
    }
    
    public void sendMessage(Socket socket, String message, String name, 
            String color, boolean sendCryptoStart) throws IOException {
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
            } catch (Exception ex) {
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
    
    public boolean hasCrypto(Socket socket) {
        return cryptos.containsKey(socket);
    }
    
    public Crypto getCrypto(Socket socket) {
        return cryptos.get(socket);
    }
    
    public void setCrypto(Socket socket, String type) throws Exception {
        cryptos.put(socket, new Crypto(type));
    }
    
    public void deleteCrypto(Socket socket) {
        cryptos.remove(socket);
    }
    
    public void setName(Socket socket, String name) {
        names.put(socket, name);
    }
    
    public String getName(Socket socket) {
        if(names.containsKey(socket)) {
            return names.get(socket);
        }
        return socket.getInetAddress().toString();
    }
    
    public void sendFileTransferRequest(String fileName, String size, 
            String message) {
    }
    
    public void replyFileTransfer(boolean answer, String reason, String port) {
        
    }
    
    public void sendKeyRequest(Socket socket, String message, String name, 
            String type) throws UnsupportedEncodingException, IOException {
        message = StringEscapeUtils.escapeHtml3(message);
 
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constants.MESSAGE_NAME).append(name).append(">")
                .append(Constants.KEY_REQUEST).append(type).append(">")
                .append(message).append(Constants.KEY_REQUEST_STOP)
                .append(Constants.MESSAGE_STOP);
        send(socket, stringBuilder.toString());

    }
    
    public void sendKey(Socket socket, String name) 
            throws UnsupportedEncodingException, IOException {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constants.MESSAGE_NAME).append(name).append(">")
                .append(Constants.KEY_RESPONSE)
                .append(getCrypto(socket).getType()).append(" key=")
                .append(getCrypto(socket).getKey()).append(">")
                .append(Constants.KEY_RESPONSE_STOP)
                .append(Constants.MESSAGE_STOP);
        send(socket, stringBuilder.toString());

    }
    
    public void sendJoinReply(Socket socket, String ans) 
            throws IOException {
        String outgoing = Constants.REQUEST_ANS + ans +">" 
                 + Constants.REQUEST_STOP;
        send(socket, outgoing);
    }
    
    public void sendJoinRequest(String message) 
            throws IOException {
        for(Socket socket: sockets) {
            String outgoing = Constants.REQUEST + message + Constants.REQUEST_STOP;
            send(socket, outgoing);
        }
    }
    
    private void send(Socket socket, String message) 
            throws UnsupportedEncodingException, IOException {
        OutputStreamWriter output =new OutputStreamWriter(
                                    socket.getOutputStream(), "UTF-8");
        output.write(message, 0, message.length());
        output.flush();
    }
    

}
