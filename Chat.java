package Chat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joar
 */
public class Chat {

    /**
     *Main function that starts the chat window
     * @param args no arguments
     */
    public static void main(String[] args) {
        String[] i ="/home/joar/NetBeansProjects/Chat/home/joar/dasakLabW-1".split("/");
        System.out.println(i[i.length-1]);
        try {
            SetupWindow setup = new SetupWindow();
                    
        } catch (Exception e) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, e);
        }

    }
    
}
