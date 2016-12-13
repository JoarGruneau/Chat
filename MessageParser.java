package Chat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringEscapeUtils;

public class MessageParser implements Runnable {
    private Controller controller; 
    private InputStreamReader input;
    private String message;
    private Conversation conversation;
    private Socket socket;
    private Connection connection;

    public MessageParser(Socket socket, Conversation conversation,
            Controller controller, Connection connection) throws IOException {
        this.socket = socket;
        this.input = new InputStreamReader(socket.getInputStream(), "UTF-8");
        this.conversation = conversation;
        this.connection = connection;
        
    }
    
    @Override
    public void run() {
        while(true) {
            String name=getName();
            if(!name.equals("")) {
                connection.setName(socket, name);
            }
            if(message == null) {
                System.out.println("connection lost");
                break;
            }
            else if(hasTags(Constants.FILE_START, Constants.FILE_STOP)) {
                
            }
            else {
                if(hasTags(Constants.ENCRYPTED_TYPE, 
                        Constants.ENCRYPTED_STOP)) {
                    if(message.contains("key")) {
                        encryptionStart();
                    }
                    else {
                        decodeEncryption();
                    }
                }
                
                String color = getColor();
                conversation.addMessage(message, name, color);
                if(connection.multiConversation) {
                    try {
                        Server server = (Server) connection;
                        server.sendOtherClients(
                                socket, StringEscapeUtils.unescapeHtml3(message), 
                                name, color);
                        
                    } catch (IOException ex) {}
                }
            }
        }
    }
    
    private String getName() {
        StringBuilder tmpMessage = new StringBuilder();
        message = null;
        
        try {
            int b = input.read();
            while (b != -1) {
                tmpMessage.append((char)b);
                message = tmpMessage.toString();
                System.out.println(message);

                //no name
                if(hasTags(Constants.MESSAGE_START, Constants.MESSAGE_STOP)) {
                    removeTags(Constants.MESSAGE_START, Constants.MESSAGE_STOP);
                    return "";
                }
                //name
                else if(hasTags(Constants.MESSAGE_NAME, 
                        Constants.MESSAGE_STOP)) {
                    removeTags(Constants.MESSAGE_NAME,Constants.MESSAGE_STOP);
                    System.out.println(message);
                    return splitFirst(">");
                }
                else if(message.contains(Constants.MESSAGE_STOP)){
                    message =Constants.BROKEN;
                    return "";
                }
                b = input.read();
            }
        }
        catch (Exception e) {
            Logger.getLogger(MessageParser.class.getName())
                          .log(Level.SEVERE, null, e);
        }
        return "";
    }


    private String getColor() {
        
        if(hasTags(Constants.TEXT_START, Constants.TEXT_STOP)){
            removeTags(Constants.TEXT_START, Constants.TEXT_STOP);
            return "";
        }
        
        else if(hasTags(Constants.TEXT_COLOR, Constants.TEXT_STOP)) {
            removeTags(Constants.TEXT_COLOR, Constants.TEXT_STOP);
            return splitFirst(">");
        }
        else{
            message = Constants.BROKEN;
            return "";
        }
    }
    
    private String splitFirst(String str) {
        int index = message.indexOf(str);
        
        if(index == -1) {
            message = Constants.BROKEN;
            return "";
        }
        String name = message.substring(0, index);
        message = message.substring(index + 1);
        return name;
    }
    
    private void removeTags(String startTag, String endTag) {
        int index = message.indexOf(endTag);
        if(index+ endTag.length() != message.length() || index == -1) {
            message = Constants.BROKEN;
            return;
        }
        message = message.substring(startTag.length(), index);
    }
    
    private boolean hasTags(String startTag, String endTag) {
            return message.startsWith(startTag) && message.contains(endTag)
                    && message.indexOf(endTag) 
                    + endTag.length() == message.length();
    
    }
        
    private void parseFileRequest() {
        removeTags(Constants.FILE_START, Constants.FILE_STOP);
        String info = splitFirst(">");
        info = info.replace("size=", "");
        String[] nameSize = info.split(" ");
    }
    private void encryptionStart() {
        try {
            removeTags(Constants.ENCRYPTED_TYPE, Constants.ENCRYPTED_STOP);
            String info = splitFirst(">");
            String[] typeAndKey = info.split(" key=");
            connection.setCrypto(socket, typeAndKey[0]);
            Crypto crypto = connection.getCrypto(socket);
            crypto.setKey(typeAndKey[1]);
            message = crypto.decodeMessage(message);
            conversation.addInfo("A new encryption of type: " 
                    + crypto.getType() + " initiated by: " 
                    + connection.getName(socket));
        } catch (Exception ex) {
            message = Constants.BROKEN_ENCRYPTION;
        }
    }
    private void decodeEncryption() {
        try{
            removeTags(Constants.ENCRYPTED_TYPE, Constants.ENCRYPTED_STOP);
            splitFirst(">");
            Crypto crypto = connection.getCrypto(socket);
            message = crypto.decodeMessage(message);
        }
        catch (Exception ex) {
            message = Constants.BROKEN_ENCRYPTION;
        }
        
    }
    
    private void parseJoinRequest() {
        
    }
    private void parseDissconect() {
        
    }
}