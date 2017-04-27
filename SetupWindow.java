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

/**
 * Setup window for Chat program
 *
 * @author joar
 */
public class SetupWindow extends JFrame {

    private JPanel masterPanel;
    private JPanel runPanel;
    private JPanel infoPanel;
    private JCheckBox runAsServer;
    private JCheckBox runAsClient;
    private JCheckBox multiConversation;
    private JButton connect;
    private JTextField ip;
    private JTextField port;

    /**
     * Creates a setup window that lets the user decide if it wants to run as a
     * server/client and if it should be a multi conversation.
     */
    public SetupWindow() {
        masterPanel = new JPanel();
        runPanel = new JPanel();
        infoPanel = new JPanel();
        runAsServer = new JCheckBox("Run as server");
        runAsClient = new JCheckBox("Run as Client");
        multiConversation = new JCheckBox("Multi conversation");
        ip = new JTextField("127.0.0.1");
        port = new JTextField("2222");
        connect = new JButton("Connect");
        connect.setBackground(Color.green);
        connect.setEnabled(false);

        connect.addActionListener((ActionEvent e) -> {
            if (runAsClient.isSelected()) {

                try {
                    Client client = new Client(ip.getText(),
                            Integer.parseInt(port.getText()));
                } catch (NumberFormatException | IOException exception) {
                    JFrame frame = new JFrame();
                    JOptionPane.showMessageDialog(frame,
                            "Could not conect to server" + exception.toString());
                }
            } else {
                try {
                    Server server = new Server(Integer.parseInt(
                            port.getText()), multiConversation.isSelected());
                } catch (Exception exception) {
                    JFrame frame = new JFrame();
                    JOptionPane.showMessageDialog(frame,
                            "Could not start server");
                }
            }
        });

        runAsClient.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                runAsServer.setSelected(false);
                connect.setEnabled(true);
                ip.setEnabled(true);
                multiConversation.setSelected(false);
                multiConversation.setEnabled(false);
            } else {
                multiConversation.setEnabled(true);
                runAsServer.setSelected(true);
                ip.setEnabled(false);
            }
        });

        runAsServer.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                runAsClient.setSelected(false);
                connect.setEnabled(true);
            } else {
                runAsClient.setSelected(true);
            }
        });

        ip.setPreferredSize(new Dimension(200, 20));
        port.setPreferredSize(new Dimension(200, 20));
        ip.setEnabled(false);
        port.setEnabled(true);

        runPanel.add(runAsClient);
        runPanel.add(runAsServer);
        runPanel.add(multiConversation);
        runPanel.add(connect);
        masterPanel.add(runPanel);
        masterPanel.setPreferredSize(new Dimension(550, 100));

        infoPanel.add(ip);
        infoPanel.add(port);
        masterPanel.add(infoPanel);
        SetupWindow.this.add(masterPanel);
        SetupWindow.this.pack();
        SetupWindow.this.setVisible(true);
        SetupWindow.this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

}
