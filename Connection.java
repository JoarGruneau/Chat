package Chat;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.StringEscapeUtils;

public abstract class Connection {
    protected HashMap<Socket, Crypto> cryptos = new HashMap();
    protected ArrayList<Socket> sockets = new ArrayList();
    protected HashMap<Socket, String> names = new HashMap();
    boolean multiConversation = false;
    
    public void sendMessage(Socket socket, String message, String name, 
            String color, boolean sendCryptoStart) throws IOException {
        message = StringEscapeUtils.escapeHtml3(message);
        OutputStreamWriter output =new OutputStreamWriter(
                                    socket.getOutputStream(), "UTF-8");
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
        String stringOutgoing =outgoing.toString();
        output.write(stringOutgoing, 0, stringOutgoing.length());
        output.flush();
        socket.getOutputStream().flush();
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
    
    public void sendKeyRequest(String message) {
        
    }
    
    public void replyKeyRequest(String key) {
        
    }

}
