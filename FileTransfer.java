package Chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileTransfer {
    private Conversation conversation;
    private boolean encrypted = false;
    private Crypto crypto;
    
    public  FileTransfer(Conversation conversation) {
        this.conversation = conversation;
        this.encrypted = false;
    }
    
    public FileTransfer(Conversation conversation, Crypto crypto) {
        this.conversation = conversation;
        this.crypto = crypto;
        this.encrypted = true;
    }
    
    public void receiveFile(String fileName, String port) {
        Runnable receive = () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
                Socket socket = serverSocket.accept();
                InputStream input = socket.getInputStream();
                if(encrypted) {
                    input = crypto.getDecryptStream(input);
                }
                FileOutputStream output = new FileOutputStream(fileName);
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
        };
        Thread receiveThread = new Thread(receive);
        receiveThread.start();
        
                
    }
    
    public void sendFile(File file, InetAddress ip, String port) {
        Runnable send = () -> {
            try {
                Socket socket = new Socket(ip, Integer.parseInt(port));
                InputStream input = new FileInputStream(file);
                OutputStream output = socket.getOutputStream();
                if(encrypted) {
                    output = crypto.getEncryptStream(output);
                }
                int count;
                byte[] buffer = new byte[8192];
                while ((count = input.read(buffer)) > 0) {
                    output.write(buffer, 0, count);
                }
                output.close();
                input.close();
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, ex);
            }
//            catch (Exception e) {
//                conversation.addInfo("Could not send file");
//                System.out.println(e.toString());
//            }
        };
        Thread sendThread = new Thread(send);
        sendThread.start();

    }
}

