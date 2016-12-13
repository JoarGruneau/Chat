package Chat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SetupWindow extends JFrame {
    private JPanel masterPanel;
    private JPanel runPanel;
    private JPanel infoPanel;
    private JCheckBox runAsServer;
    private JCheckBox runAsClient;
    private JButton connect;
    private JTextField ip;
    private JTextField port;
    
    public SetupWindow() {
        masterPanel = new JPanel();
        runPanel= new JPanel();
        infoPanel = new JPanel();
        runAsServer = new JCheckBox("Run as server");
        runAsClient = new JCheckBox("Run as Client");
        ip = new JTextField("127.0.0.1");
        port = new JTextField("2222");
        connect = new JButton("Connect");
        connect.setBackground(Color.green);
        connect.setEnabled(false);
        
        connect.addActionListener((ActionEvent e) -> {
            if(runAsClient.isSelected()) {
                
                try{
                    Client client = new Client(ip.getText(), 
                            Integer.parseInt(port.getText()));
                }
                
                catch(NumberFormatException | IOException exception){
                    JFrame frame=new JFrame();
                    JOptionPane.showMessageDialog(frame,
                            "Could not conect to server" + exception.toString());
                }
            }
            
            else {
                try{
                    Server server = new Server(Integer.parseInt(
                            port.getText()), true);
                }
                catch(Exception exception){
                    JFrame frame=new JFrame();
                    JOptionPane.showMessageDialog(frame,
                            "Could not start server");
                }
            }
        });
        
        runAsClient.addItemListener((ItemEvent e) -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                runAsServer.setEnabled(false);
                connect.setEnabled(true);
                ip.setEnabled(true);
            }
            else{
                runAsServer.setEnabled(true);
                connect.setEnabled(false);
                ip.setEnabled(false);
            }
        });
        
        runAsServer.addItemListener((ItemEvent e) -> {
            if(e.getStateChange() == ItemEvent.SELECTED) {
                runAsClient.setEnabled(false);
                connect.setEnabled(true);
            }
            else{
                runAsClient.setEnabled(true);
                connect.setEnabled(false);
            }
        });
        
        ip.setPreferredSize(new Dimension(200,20));
        port.setPreferredSize(new Dimension(200,20));
        ip.setEnabled(false);
        port.setEnabled(true);
        
        runPanel.add(runAsServer);
        runPanel.add(runAsClient);
        runPanel.add(connect);
        masterPanel.add(runPanel);
        masterPanel.setPreferredSize(new Dimension(500,100));
        
        infoPanel.add(ip);
        infoPanel.add(port);
        masterPanel.add(infoPanel);
        SetupWindow.this.add(masterPanel);
        SetupWindow.this.pack();
        SetupWindow.this.setVisible(true);
        SetupWindow.this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    }
    
}
