package Chat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import org.apache.commons.lang3.StringEscapeUtils;

public class MessageParser implements Runnable {
    private Controller controller; 
    private InputStreamReader input;
    private String message;
    private Conversation conversation;
    private Socket socket;
    private Connection connection;
    private boolean accepted;

    public MessageParser(Socket socket, Conversation conversation,
            Controller controller, Connection connection, boolean accepted) 
            throws IOException {
        this.accepted = accepted;
        this.socket = socket;
        this.input = new InputStreamReader(socket.getInputStream(), "UTF-8");
        this.controller = controller;
        this.conversation = conversation;
        this.connection = connection;
        
    }
    
    @Override
    public void run() {
        while(true) {
            String name = getName();
            if(!name.equals("")) {
                connection.setName(socket, name);
            }
            if(message == null) {
                conversation.addInfo("Lost connection with: " + 
                        connection.getName(socket));
                connection.sockets.remove(socket);
                break;
            }
            
            else if(hasTags("", Constants.DISCONNECT)) {
                conversation.addInfo(connection.getName(socket) 
                        + " has loged off");
                connection.sockets.remove(socket);
                break;
                
            }
            
            else if(!accepted) {
                if(hasTags(Constants.REQUEST, Constants.REQUEST_STOP)) {
                    parseJoinRequest();
                    controller.handelJoinRequest(socket, message, false);
                }
                else {
                    controller.handelJoinRequest(socket, 
                            "A primitive chat application is trying to connect", 
                            true);
                }
                accepted = true;
                continue;
            }
            
            if(hasTags(Constants.ENCRYPTED_TYPE, 
                        Constants.ENCRYPTED_STOP)) {
                    if(message.contains("key")) {
                        encryptionStart();
                    }
                    else {
                        decodeEncryption();
                    }
            }
                
            if(message.equals(Constants.BROKEN_ENCRYPTION)) {
                conversation.addMessage(message,
                        connection.getName(socket), "");
            }
                
            
            else if(hasTags(Constants.REQUEST_ANS, Constants.REQUEST_STOP)) {
                String ans = parseJoinReply();
                conversation.addInfo("Answer to join request: " + ans);
            }
            
            else if(hasTags(Constants.KEY_REQUEST, 
                    Constants.KEY_REQUEST_STOP)) {
                parseKeyRequest();
                
            }
            
            else if(hasTags(Constants.KEY_RESPONSE, 
                    Constants.KEY_RESPONSE_STOP)) {
                parseKeyResponse(); 
            }
            
            else if(hasTags(Constants.FILE_REQUEST_NAME, 
                    Constants.FILE_REQUEST_STOP)) {
                parseFileRequest();
               
            }
            
            else if(hasTags(Constants.FILE_RESPONES, 
                    Constants.FILE_RESPONSE_STOP)) {
                parseFileResponse();
                
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
                
                if(message.equals(Constants.BROKEN_ENCRYPTION)) {
                    conversation.addMessage(message,
                            connection.getName(socket), "");
                    continue;
                }
                
                String color = getColor();
                System.out.println("hej" + message);
                conversation.addMessage(message, name, color);
                if(connection.multiConversation) {
                    try {
                        connection.sendOtherClients(
                                socket, StringEscapeUtils.unescapeHtml3(message), 
                                name, color);
                        
                    } 
                    catch (Exception ex) {}
                }
            }
        }
    }
    
    private String getName() {
        StringBuilder tmpMessage = new StringBuilder();
        message = null;
        
        try {
            int tmpChar = input.read();
            while (tmpChar != -1) {
                tmpMessage.append((char) tmpChar);
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
                    String name = getAtribute("name");
                    removeTags(Constants.MESSAGE_NAME,Constants.MESSAGE_STOP);
                    splitFirst(">");
                    return name;
                }
                else if(message.contains(Constants.MESSAGE_STOP)){
                    message =Constants.BROKEN;
                    return "";
                }
                tmpChar = input.read();
            }
        }
        catch (Exception e) {
            message = null;
        }
        return "";
    }

    private String getColor() {
        
        if(hasTags(Constants.TEXT_START, Constants.TEXT_STOP)){
            removeTags(Constants.TEXT_START, Constants.TEXT_STOP);
            return "";
        }
        
        else if(hasTags(Constants.TEXT_COLOR, Constants.TEXT_STOP)) {
            String color = getAtribute("color");
            removeTags(Constants.TEXT_COLOR, Constants.TEXT_STOP);
            splitFirst(">");
            return color;
        }
        else{
            message = Constants.BROKEN;
            return "";
        }
    }
    
    
    private String getAtribute(String atribute) {
        atribute = atribute + "=";
        if(message.contains(atribute) && 
                message.indexOf(atribute) < message.indexOf(">")) {
            int index = message.indexOf(atribute) + atribute.length();
            int end;
            
            if(message.indexOf(" ", index) != -1 && 
                    message.indexOf(" ", index) < message.indexOf(">", index)) {
                end = message.indexOf(" ", index);
            }
            else {
                end = message.indexOf(">", index);
            }
            if (index == end) {
                return "";
            }
            
            else {
                String name = message.substring(index, end);
                return name;
            }
        }
        else {
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
    
    private void parseKeyRequest() {
        String type = getAtribute("type");
        removeTags(Constants.KEY_REQUEST, Constants.KEY_REQUEST_STOP);
        splitFirst(">");
        controller.keyRequest(socket, message, connection.getName(socket), type);
        
    }
    
    private void parseKeyResponse() {
        String type = getAtribute("type");
        String key = getAtribute("key");
        removeTags(Constants.KEY_RESPONSE, Constants.KEY_RESPONSE_STOP);
        splitFirst(">");
        controller.handleKeyReply(socket, type, key);
    }
        
    private void parseFileRequest() {
        String fileName = getAtribute("name");
        String size = getAtribute("size");
        removeTags(Constants.FILE_REQUEST_NAME, Constants.FILE_REQUEST_STOP);
        splitFirst(">");
        controller.handleFileRequest(socket, message, fileName, size);   
    }
    
    private void parseFileResponse() {
        String answer = getAtribute("reply");
        String port = getAtribute("port");
        String type = getAtribute("type");
        String key = getAtribute("key");
        removeTags(Constants.FILE_RESPONES, Constants.FILE_RESPONSE_STOP);
        splitFirst(">");
        controller.handleFileResponse(socket, message, answer, 
                    port, type, key);
    }
    private void encryptionStart() {
        try {
            String type = getAtribute("type");
            String key = getAtribute("key");
            removeTags(Constants.ENCRYPTED_TYPE, Constants.ENCRYPTED_STOP);
            splitFirst(">");
            connection.setCrypto(socket, type);
            Crypto crypto = connection.getCrypto(socket);
            crypto.setKey(key);
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
        removeTags(Constants.REQUEST, Constants.REQUEST_STOP);
    }
    
    private String parseJoinReply() {
        String ans = getAtribute("ans");
        removeTags(Constants.REQUEST_ANS, Constants.REQUEST_STOP);
        splitFirst(">");
        return ans;
    }

}