package Chat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chat {

    public static void main(String[] args) {
        try {
            SetupWindow setup = new SetupWindow();
//            Crypto test = new Crypto("CAESAR");
//            Crypto tt2 = new Crypto("AES");
//            String key =test.generateKey();
//            System.out.println(key);
//            tt2.setKey(key);
//            System.out.println(test.encodeMessage("hej på dig"));
//            System.out.println(tt2.encodeMessage("hej på dig"));
//            System.out.println(test.decodeMessage(test.encodeMessage("hej på dig")));
                    
        } catch (Exception e) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, e);
        }

    }
    
}
