package Chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.StringEscapeUtils;

public abstract class Connection {
    protected HashMap<Socket, Crypto> cryptos = new HashMap();
    protected ArrayList<Socket> sockets = new ArrayList();
    protected HashMap<Socket, String> names = new HashMap();
    protected boolean multiConversation = false;
    private static int port = 1025;
    
    public String getPort() {
        port++;
        if(port > 65534) {
            port = 1025;
        }
        return Integer.toString(port);
    }
    
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
    
    public void sendFileTransferRequest(Socket socket, String message,  
            String name, String fileName, String size) throws IOException {
        message = StringEscapeUtils.escapeHtml3(message);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constants.MESSAGE_NAME).append(name).append(">")
                .append(Constants.FILE_REQUEST_NAME).append(fileName)
                .append(" size=").append(size).append(">")
                .append(message).append(Constants.FILE_REQUEST_STOP)
                .append(Constants.MESSAGE_STOP);
        send(socket, stringBuilder.toString());
    }
    
    public void replyFileTransfer(Socket socket, String name, String answer, 
            String reason, String port) throws IOException {
        reason = StringEscapeUtils.escapeHtml3(reason);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Constants.MESSAGE_NAME).append(name).append(">")
                .append(Constants.FILE_RESPONES).append(answer)
                .append(" port=").append(port).append(">")
                .append(reason).append(Constants.FILE_RESPONSE_STOP)
                .append(Constants.MESSAGE_STOP);
        send(socket, stringBuilder.toString());
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
    
    public void sendJoinReply(Socket socket, String name, String ans) 
            throws IOException {
        String outgoing = Constants.MESSAGE_NAME + name + ">" 
                + Constants.REQUEST_ANS + ans +">" 
                 + Constants.REQUEST_STOP + Constants.MESSAGE_STOP;
        send(socket, outgoing);
    }
    
    public void sendJoinRequest(String message, String name) 
            throws IOException {
        for(Socket socket: sockets) {
            String outgoing = Constants.MESSAGE_NAME + name + ">" 
                    + Constants.REQUEST + message + Constants.REQUEST_STOP + 
                    Constants.MESSAGE_STOP;
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
    
    public void receiveFile(String fileName, String size, 
            String port) throws Exception {
        
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
        Socket socket = serverSocket.accept();
        InputStream input = socket.getInputStream();
        OutputStream output = new FileOutputStream(fileName);
        int count;
        byte[] buffer = new byte[8192];
        while ((count = input.read(buffer)) > 0) {
            output.write(buffer, 0, count);
        }
        serverSocket.close();
        socket.close();
        input.close();
        output.close();
    }
    
    public void sendFile(String fileName, String size, String ip, String port)  
            throws Exception{
        Socket socket = new Socket(ip, Integer.parseInt(port));
        File file = new File(fileName);
        InputStream input = new FileInputStream(file);
        OutputStream output = socket.getOutputStream();
        int count;
        byte[] buffer = new byte[8192];
        while ((count = input.read(buffer)) > 0) {
            output.write(buffer, 0, count);
        }
        output.close();
        input.close();
        socket.close();
    }
    

}
