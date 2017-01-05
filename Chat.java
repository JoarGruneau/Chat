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
        try {
            SetupWindow setup = new SetupWindow();
                    
        } catch (Exception e) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, e);
        }

    }
    
}
