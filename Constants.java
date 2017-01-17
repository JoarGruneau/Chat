package Chat;

import java.awt.Color;

/**
 *The message constants
 * @author joar
 */
public class Constants {
        public static final String MESSAGE_START = "<message>";
        
        public static final String MESSAGE_NAME = "<message name=";
        
        public static final String MESSAGE_STOP = "</message>";
        
        public static final String BROKEN = "broken message";
        
        public static final String TEXT_START = "<text>";
        
        public static final String TEXT_COLOR = "<text color=";
        
        public static final String TEXT_STOP = "</text>";
        
        public static final String BOLD_START = "<fetstil>";
        
        public static final String BOLD_STOP = "</fetstil>";
        
        public static final String KURSIVE_START = "<kursive>";
        
       public static final String KURSIVE_STOP = "</kursive>";
        
        public static final String KEY_REQUEST = "<keyrequest type=";
        
        public static final String KEY_REQUEST_STOP = "</keyrequest>";
        
        public static final String KEY_RESPONSE = "<keyresponse type=";
        
        public static final String KEY_RESPONSE_STOP = "</keyresponse>";
        
        public static final String ENCRYPTED_STOP = "</encrypted>";
        
        public static final String ENCRYPTED_TYPE = "<encrypted type=";
        
        public static final String FILE_REQUEST_NAME = "<filerequest name=";
        
        public static final String FILE_REQUEST_STOP = "</filerequest>";
        
        public static final String FILE_RESPONES = "<fileresponse reply=";
        
        public static final String FILE_RESPONSE_STOP = "</fileresponse>";
        
        public static final String REQUEST = "<request>";
        
        public static final String REQUEST_ANS = "<request ans=";
        
        public static final String REQUEST_STOP = "</request>";
        
        public static final String DISCONNECT = "<disconnect/>";
        
        public static final String BROKEN_ENCRYPTION  = 
                "broken encrypted message";
        
        public static final String[] ENCRYPTIONS = {"AES", "CAESAR"};
        
        public static final Color[] colorList = {Color.BLUE, Color.CYAN, 
            Color.DARK_GRAY, Color.GREEN, Color.MAGENTA, Color.ORANGE,
            Color.PINK, Color.RED, Color.YELLOW};
}
