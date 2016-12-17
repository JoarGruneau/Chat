
package Chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Observable;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import org.apache.commons.lang3.StringEscapeUtils;


public class  Controller extends JPanel {
    private Waiters keyWaiters;
    private Waiters fileWaiters;
    private boolean sendKeyRequest;
    private String keyType;
    private boolean singleConnection;
    private Connection connection;
    private Conversation conversation;
    private Socket choosenSocket;
    private String color = "";
    private JTextArea chatField;
    private JPanel textPanel;
    private JButton send;
    private JScrollPane scrollPane;
    private JScrollBar vertical;
    private JPanel buttonPanel;
    private JMenuBar colorBar;
    private JMenu colorMenu;
    private JMenuBar fileBar;
    private JTextField nameField;
    private JMenuItem[] colorItems;
    private JMenu[] encryptions;
    private JMenuItem[] cryptoItems;
    private JLabel nameLabel;
    private JMenu sendFile;
    private JFileChooser fileChooser;
    private JMenu encryptionMenu;
    private JMenuBar encryptionBar;
    private boolean sendCryptoStart = false;
    private boolean accepted;
    private boolean fileTransfer = false;
    private File file;
    
    public Controller(View view, Conversation conversation, 
            Connection connection, boolean accepted) {
        this(view, conversation, connection, accepted, null);
        this.singleConnection = false;
       
   }
    public Controller(View view, Conversation conversation, 
            Connection connection, boolean accepted, Socket socket) {
        
        this.accepted = accepted;
        this.connection = connection;
        this.singleConnection = true;
        this.conversation = conversation;
        keyWaiters = new Waiters();
        fileWaiters = new Waiters();
        
        conversation.addObserver((Observable o, Object arg) -> {
            scrollPane.revalidate();
            vertical.setValue( vertical.getMaximum() );
            scrollPane.repaint();
            updateEncryptions();
            updateSendFile();
        });
        
        Controller.this.setLayout(new BorderLayout());
        scrollPane = new JScrollPane(view);
        this.vertical = scrollPane.getVerticalScrollBar();
        scrollPane.setPreferredSize(new Dimension(500, 500));
        Controller.this.add(scrollPane);
        
        buttonPanel = new JPanel();
        nameLabel = new JLabel("Enter name:");
        nameField = new JTextField("");
        nameField.setPreferredSize(new Dimension(80,20));
        buttonPanel.add(nameLabel);
        buttonPanel.add(nameField);
        
        colorBar = new JMenuBar();
        colorMenu = new JMenu("Set color");
        colorMenu.setPreferredSize(new Dimension(80, 20));
        colorMenu.setBackground(Color.LIGHT_GRAY);
        colorItems = new JMenuItem[Constants.colorList.length];
        
        for(int i = 0; i<Constants.colorList.length; i++) {
            colorItems[i] = new JMenuItem();
            colorItems[i].setActionCommand(toHex(Constants.colorList[i]));
            colorItems[i].addActionListener((ActionEvent ev) -> {
                color = ev.getActionCommand();
                chatField.setForeground(Color.decode(color));
            });
            colorItems[i].setBackground(Constants.colorList[i]);
            colorMenu.add(colorItems[i]);
        }
        colorBar.add(colorMenu);
        buttonPanel.add(colorBar);
        
        encryptionBar = new JMenuBar();
        encryptionMenu = new JMenu("Encryption");
        
        encryptions = new JMenu[Constants.ENCRYPTIONS.length * 2];
        for(int i = 0; i < Constants.ENCRYPTIONS.length; i++) {
            encryptions[i * 2] = new JMenu(Constants.ENCRYPTIONS[i]);
            encryptions[i*2 + 1] = new JMenu("Key Request: " + 
                    Constants.ENCRYPTIONS[i]);
            encryptions[i*2 + 1].setActionCommand(Constants.ENCRYPTIONS[i]);
            encryptionMenu.add(encryptions[i*2]);
            encryptionMenu.add(encryptions[i*2 + 1]);
        }
        updateEncryptions();
        encryptionBar.add(encryptionMenu);
        buttonPanel.add(encryptionBar);
        
        sendFile = new JMenu("Send File");
        updateSendFile();
        fileBar = new JMenuBar();
        fileBar.add(sendFile);
        buttonPanel.add(fileBar);
        
        Controller.this.add(buttonPanel);
        textPanel = new JPanel();
        chatField = new JTextArea();
        send = new JButton();
        if(accepted) {
            send.setText("Send");
        }
        else {
            send.setText("Send join request");
        }
        chatField.setForeground(Color.BLACK);
        chatField.setPreferredSize(new Dimension(500, 100));
        textPanel.add(chatField);
        textPanel.add(send);
        Controller.this.add(textPanel);
        Controller.this.setPreferredSize(new Dimension(600, 650));
        Controller.this.setLayout(new FlowLayout(FlowLayout.LEADING));

        send.addActionListener((ActionEvent e) -> {
            try {
                if(!this.accepted) {
                    connection.sendJoinRequest(chatField.getText(), 
                            nameField.getText());
                    send.setText("Send");
                    this.accepted = true;
                }
                else if(singleConnection) {
                    if(sendKeyRequest) {
                        connection.sendKeyRequest(choosenSocket, 
                                    chatField.getText(),nameField.getText(), 
                                    keyType);
                        keyWaiters.addWaiter(choosenSocket, 
                                "Did not receive key from: ");
                    }
                    else if(fileTransfer) {
                        connection.sendFileTransferRequest(choosenSocket, 
                                chatField.getText(), nameField.getText(), 
                                file.toString(), "" + file.length());
                        fileWaiters.addWaiter(choosenSocket, 
                                "Did not receive file response from ");
                        fileTransfer = false;
                    }
                    else {
                        connection.sendMessage(socket, chatField.getText(), 
                            nameField.getText(), color, sendCryptoStart);
                        conversation.addMessage(StringEscapeUtils.escapeHtml3(
                            chatField.getText()), nameField.getText(), color);
                    }
                }
                else {
                    if(sendCryptoStart) {
                        connection.sendMessage(choosenSocket, 
                                chatField.getText(), 
                                nameField.getText(), color, true);
                        connection.sendOtherClients(choosenSocket,
                                chatField.getText(), 
                                nameField.getText(), color);
                    }
                    
                    else if(fileTransfer) {
                        connection.sendFileTransferRequest(choosenSocket, 
                                chatField.getText(), nameField.getText(), 
                                file.getName(), "" + file.length());
                        fileWaiters.addWaiter(choosenSocket, 
                                "Did not receive file response from ");
                        fileTransfer = false;
                    }
                    
                    else if(sendKeyRequest) {
                        connection.sendKeyRequest(choosenSocket, 
                                    chatField.getText(),nameField.getText(), 
                                    keyType);
                        keyWaiters.addWaiter(choosenSocket, 
                                "Did not receive key from: ");
                    }
                    else {
                        connection.sendMessage(chatField.getText(), 
                            nameField.getText(), color, false);
                        conversation.addMessage(StringEscapeUtils.escapeHtml3(
                            chatField.getText()), nameField.getText(), color);
                    }
                }
                fileTransfer = false;
                sendKeyRequest = false;
                sendCryptoStart = false;
            } catch (IOException ex) {
                conversation.addInfo("Could not send message");
            }
           chatField.setText("");
       });
   }
    public void keyRequest(Socket socket, String message, 
            String name, String type) {
        
        if (JOptionPane.showConfirmDialog(null, "Do you want to send your " + 
                type + " key  to " + name + ":\n"
              + "Reason for key request: " + message,  "KEY REQUEST",
        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try{
                if(connection.hasCrypto(socket) && 
                        connection.getCrypto(socket).getType().equals(type)) {
                    connection.sendKey(socket, nameField.getText());
                }
                else {
                    connection.setCrypto(socket, type);
                    connection.sendKey(socket, nameField.getText());
                }
           } catch (Exception e) {
                        conversation.addInfo("Could not send key to " + name);
            }
        };
    }
    
    public void handleKeyReply(Socket socket, String type, String key) {
        if(keyWaiters.isWaiting(socket)) {
            keyWaiters.stopTimer(socket);
            conversation.addInfo("Received key from " + 
                    connection.getName(socket));
            try {
                connection.setCrypto(socket, type);
                Crypto crypto = connection.getCrypto(socket);
                crypto.setKey(key);
                conversation.addInfo("Initiated new crypto with received key");
            } catch (Exception ex) {
                conversation.addInfo("Could not use received key from " + 
                    connection.getName(socket));
            }

        }
        else {
            conversation.addInfo("Received unexpected key reply from " + 
                    connection.getName(socket));
        }
    }
    public void fileReply(String file, String message, String size) {
        JOptionPane optionPane = new JOptionPane(
            "Do you want to accept transfer of:\n"
            + "File: " + file + "\n"+ "Size: " + size + "\n"
            + "Message; " + message, JOptionPane.QUESTION_MESSAGE,
            JOptionPane.YES_NO_OPTION);
    }
    
    
    public void handelJoinRequest(Socket socket, String message, 
            boolean primitive) {
        String ans = "no";
        if (JOptionPane.showConfirmDialog(null, "Do you want to allow " + 
                connection.getName(socket) + " to join the conversation\n"
              + "Reason to join: " + message,  "KEY REQUEST",
        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            ans = "yes";
            connection.sockets.add(socket);
        }
        try {
            if(primitive) {
                if(ans.equals("yes")) {
                    connection.sendMessage(socket, 
                            "You are allowed to join", "Server", "", false);
                }
                else{
                    connection.sendMessage(socket, 
                            "You are not allowed to join", "Server", "", false);
                }
            }
            else {
                connection.sendJoinReply(socket, nameField.getText(), ans);
            }
            if(ans.equals("no")) {
                socket.close();
            }
        }
        catch (Exception e) {
            conversation.addInfo("Could not reply to join request");
        }
        
    }
    
        public void handleFileResponse(Socket socket, String message, String ans,
            String port) {
        if(fileWaiters.isWaiting(socket)) {
            fileWaiters.stopTimer(socket);
            if(ans.equals("yes")) {
                conversation.addInfo("File transfer was accepted, Reason: " 
                        + message);
                FileTransfer fileTransfer = new FileTransfer(conversation);
                fileTransfer.sendFile(file, socket.getInetAddress(), port);
            }
            else {
                conversation.addInfo("File transfer request was not accepted, "
                        + "Reason: " + message);
            }
        }
        else {
             conversation.addInfo("Received unexpected file transfer response");
        }
    }
    
    public void handleFileRequest(Socket socket, String message, 
            String fileName, String size) {
        int ans = JOptionPane.showConfirmDialog(null,
                "Whould you like ot accept a file transfer from " + 
                        connection.getName(socket)  + "?\n" +
                "File name: " + fileName + "\n" +
                "Size: " + size + " bytes" + "\n" +
                "Message: " + message + "\n",   
                "File transfer request",
                JOptionPane.YES_NO_OPTION);
        String reason = JOptionPane.showInputDialog(null,
                "Giv reason for answer\n");
        if(reason == null) {
            reason = "";
        }
        String port =connection.getPort();
        try {
            if(ans == JOptionPane.YES_OPTION) {
                FileTransfer fileTransfer = new FileTransfer(conversation);
                connection.replyFileTransfer(socket, nameField.getText(), 
                        "yes", reason, port);
                fileTransfer.receiveFile(fileName, port);
            }
            else {
                connection.replyFileTransfer(socket, nameField.getText(), 
                        "no", reason, "");
            }
        }
        catch(Exception e) {
            conversation.addInfo("Could nor reply to file transfer request");
        }
    }
    
    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), 
                color.getBlue());
    }
    private void updateEncryptions() {
        for(JMenu encryption: encryptions){
            encryption.removeAll();
            for(Socket socket: connection.sockets) {
                JMenuItem tmpItem = new JMenuItem(connection.getName(socket));
                tmpItem.addActionListener((ActionEvent e) -> {
                    choosenSocket = socket;
                    if(encryption.getText().contains("Request")) {
                        sendKeyRequest = true;
                        keyType = encryption.getActionCommand();
                    }
                    else {
                        connection.deleteCrypto(socket);
                        try {
                            connection.setCrypto(socket, encryption.getText());
                            sendCryptoStart = true;
                        } catch (Exception ex) {
                            conversation.addInfo("could not set new crypto");
                        }
                    }
                });
                encryption.add(tmpItem);
            }
        }
    }
    
    private void updateSendFile() {
        sendFile.removeAll();
        for(Socket socket: connection.sockets) {
            JMenuItem tmpItem = new JMenuItem(connection.getName(socket));
            tmpItem.addActionListener((ActionEvent e) -> {
                choosenSocket = socket;
                fileChooser =new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    fileTransfer = true;
                }
            });
            sendFile.add(tmpItem);
        }
    }
    class Waiters {
        private HashMap<Socket, Timer> timers;

        public Waiters() {
            timers = new HashMap();
        }
        
        public void addWaiter(Socket socket, String info) {
            Timer timer = new Timer(1000*20, (ActionEvent e) -> {
                conversation.addInfo(info + 
                        connection.getName(socket) + " in time.");
                stopTimer(socket);
            });
            timers.put(socket, timer);
            timer.start();
        }
        
        public void stopTimer(Socket socket) {
            Timer timer = timers.get(socket);
            timer.stop();
        }
        
        public boolean isWaiting(Socket socket) {
            if(timers.containsKey(socket)) {
                return timers.get(socket).isRunning();
            }
            else {
                return false;
            }
        }
    }

}


