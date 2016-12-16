
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
    private KeyWaiters keyWaiters;
    private boolean sendKeyRequest;
    private String keyType;
    private boolean singleConnection;
    private Connection connection;
    private Conversation conversation;
    private Socket cryptoSocket;
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
    
    public Controller(View view, Conversation conversation, 
            Connection connection) {
        this(view, conversation, connection, null);
        this.singleConnection = false;
       
   }
    public Controller(View view, Conversation conversation, 
            Connection connection, Socket socket) {
        
        this.connection = connection;
        this.singleConnection = true;
        this.conversation = conversation;
        keyWaiters = new KeyWaiters();
        
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
        send = new JButton("Send");
        chatField.setForeground(Color.BLACK);
        chatField.setPreferredSize(new Dimension(500, 100));
        textPanel.add(chatField);
        textPanel.add(send);
        Controller.this.add(textPanel);
        Controller.this.setPreferredSize(new Dimension(600, 650));
        Controller.this.setLayout(new FlowLayout(FlowLayout.LEADING));

        send.addActionListener((ActionEvent e) -> {
            try {
                if(singleConnection) {
                    if(sendKeyRequest) {
                        connection.sendKeyRequest(cryptoSocket, 
                                    chatField.getText(),nameField.getText(), 
                                    keyType);
                        keyWaiters.addKeyWaiter(cryptoSocket);
                    }
                    else {
                        connection.sendMessage(socket, chatField.getText(), 
                            nameField.getText(), color, sendCryptoStart);
                    }
                }
                else {
                    if(sendCryptoStart) {
                        connection.sendMessage(cryptoSocket, 
                                chatField.getText(), 
                                nameField.getText(), color, true);
                        connection.sendOtherClients(cryptoSocket,
                                chatField.getText(), 
                                nameField.getText(), color);
                    }
                    else if(sendKeyRequest) {
                        connection.sendKeyRequest(cryptoSocket, 
                                    chatField.getText(),nameField.getText(), 
                                    keyType);
                        keyWaiters.addKeyWaiter(cryptoSocket);
                    }
                    else {
                        connection.sendMessage(chatField.getText(), 
                            nameField.getText(), color, false);
                    }
                }
                sendKeyRequest = false;
                sendCryptoStart = false;
                conversation.addMessage(StringEscapeUtils.escapeHtml3(
                   chatField.getText()), nameField.getText(), color);
            } catch (IOException ex) {
                conversation.addInfo("Could not send message");
            }
           chatField.setText("");
       });
   }
    public void keyReply(Socket socket, String message, 
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
            System.out.println(type);
            System.out.println(key);
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
    
    
    public void joinReply(String ip) {
        
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
                    cryptoSocket = socket;
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
                fileChooser =new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                }
            });
            sendFile.add(tmpItem);
        }
    }
    class KeyWaiters {
        private HashMap<Socket, Timer> timers;

        public KeyWaiters() {
            timers = new HashMap();
        }
        
        public void addKeyWaiter(Socket socket) {
            Timer timer = new Timer(1000*20, (ActionEvent e) -> {
                conversation.addInfo("No key reply received form " + 
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


