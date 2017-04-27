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
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;

/**
 * The class handling the file transfer over a new socket
 *
 * @author joar
 */
public class FileTransfer {

    private Conversation conversation;
    private boolean encrypted = false;
    private Crypto crypto;

    /**
     * Creates a file transfer
     *
     * @param conversation Conversation to add info about file transfer
     */
    public FileTransfer(Conversation conversation) {
        this.conversation = conversation;
        this.encrypted = false;
    }

    /**
     * Creates an encrypted file transfer
     *
     * @param conversation Conversation to add info about file transfer
     * @param crypto The crypto to encrypt the transfer with
     */
    public FileTransfer(Conversation conversation, Crypto crypto) {
        this.conversation = conversation;
        this.crypto = crypto;
        this.encrypted = true;
    }

    /**
     * Receives a file on a specified port (server side)
     *
     * @param fileName the name of the file
     * @param size size of the file
     * @param port the port on which to receive the file
     */
    public void receiveFile(String fileName, int size, String port) {
        ProgressMonitor progressMonitor = new ProgressMonitor(null,
                "receiving: " + fileName, "", 0, size);

        Runnable receive = () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(
                        Integer.parseInt(port));
                Socket socket = serverSocket.accept();
                InputStream input = socket.getInputStream();
                if (encrypted) {
                    input = crypto.getDecryptStream(input);
                }
                FileOutputStream output = new FileOutputStream(
                        Constants.WORKING_DIR + "/" + fileName);

                int count;
                int i = 0;
                byte[] buffer = new byte[8192];
                while ((count = input.read(buffer)) > 0) {
                    output.write(buffer, 0, count);
                    i += count;
                    progressMonitor.setProgress(i);
                }

                serverSocket.close();
                socket.close();
                input.close();
                output.close();
            } catch (Exception ex) {
                conversation.addInfo("Could not receive file");
            }
        };
        Thread receiveThread = new Thread(receive);
        receiveThread.start();

    }

    /**
     * Sends a file (client side) to a waiting server on the specified IP and
     * port.
     *
     * @param file name of the file
     * @param ip the IP of the waiting server
     * @param port the port the server listens to
     */
    public void sendFile(File file, InetAddress ip, String port) {
        ProgressMonitor progressMonitor = new ProgressMonitor(null,
                "sending: " + file.getName(),
                "", 0, (int) file.length());
        Runnable send = () -> {
            try {
                Socket socket = new Socket(ip, Integer.parseInt(port));
                InputStream input = new FileInputStream(file);
                OutputStream output = socket.getOutputStream();
                if (encrypted) {
                    output = crypto.getEncryptStream(output);
                }

                int count;
                int i = 0;
                byte[] buffer = new byte[8192];
                while ((count = input.read(buffer)) > 0) {
                    output.write(buffer, 0, count);
                    i++;
                    progressMonitor.setProgress(i * buffer.length);
                }
                output.close();
                input.close();
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(FileTransfer.class.getName())
                        .log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(FileTransfer.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        };
        Thread sendThread = new Thread(send);
        sendThread.start();

    }
}
