package Chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTransfer {
    Conversation conversation;
    
    public  FileTransfer(Conversation conversation) {
        this.conversation = conversation;
    }
    
    public void receiveFile(String fileName, String port) {
        try {
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
        catch (Exception e) {
            conversation.addInfo("Could not receive file");
        }
    }
    
    public void sendFile(File file, InetAddress ip, String port) {
        try {
            Socket socket = new Socket(ip, Integer.parseInt(port));
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
        catch (Exception e) {
            conversation.addInfo("Could not send file");
        }
    }
}
