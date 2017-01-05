package Chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.*;
import org.apache.commons.codec.binary.Hex;
import static org.apache.commons.codec.binary.Hex.decodeHex;

/**
 *
 * @author joar
 */
public class Crypto {
    private String type;
    private  Object myKey;
    private Cipher aesCipher;
    
    /**
     *Creates a new crypto of the given type
     * @param type the type of the Crypto
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public Crypto(String type) throws NoSuchAlgorithmException, 
            NoSuchPaddingException {
        if(type.equals("AES") || type.equals("CAESAR")) {
            this.type = type;
            if(type.equals("AES")) {
                aesCipher = Cipher.getInstance("AES");
                KeyGenerator KeyGen = KeyGenerator.getInstance("AES");
                KeyGen.init(128);
                myKey = KeyGen.generateKey();
            }
            else{
                myKey = (int)(Math.random()*20+1);
            }
        }
        else {
            throw new NoSuchAlgorithmException();
        }
    }
    
    /**
     *Gets the type
     * @return the type of the crypto
     */
    public String getType() {
        return type;
    }
    
    /**
     *Gets the key of the crypto
     * @return The key
     */
    public String getKey() {
        if(type.equals("AES")) {
            byte[] data = ((SecretKey) myKey).getEncoded();
            String key = Hex.encodeHexString(data);
            return key;
        }
        else {
            myKey = (int)(Math.random()*20+1);
            String key = Integer.toString((int) myKey);
            return key;
        } 
    }
    
    /**
     *Sets a key which must correspond with the type of the Crypto
     * @param key the key to be set
     * @throws DecoderException
     */
    public void setKey(String key) throws DecoderException {
        if( type.equals("AES")) {
            byte[] data = decodeHex(key.toCharArray());
            myKey = new SecretKeySpec(data, "AES");
        }
        else {
            myKey = Integer.parseInt(key);
        }
    }

    /**
     *Gets a encryption stream
     * @param output the plaintext output stream
     * @return the encrypted output stream
     * @throws Exception
     */
    public CipherOutputStream getEncryptStream(OutputStream output) throws Exception {
            aesCipher.init(Cipher.ENCRYPT_MODE, (SecretKey) myKey);
            CipherOutputStream encryptStream = new CipherOutputStream(output, aesCipher);
            return encryptStream;
    }

    /**
     *Gets a decryption stream
     * @param input the encrypted input stream
     * @return the plaintext input stream
     * @throws Exception
     */
    public CipherInputStream getDecryptStream(InputStream input) throws Exception {
            aesCipher.init(Cipher.DECRYPT_MODE, (SecretKey) myKey);

            CipherInputStream decryptStream = new CipherInputStream(input, aesCipher);
            return decryptStream;
    }
    
    /**
     *Decodes an encrypted message
     * @param hexMessage the encrypted message as hex
     * @return the plaintext
     * @throws Exception
     */
    public String decodeMessage(String hexMessage) throws Exception {
        hexMessage = hexMessage.toLowerCase();
        byte[] data = decodeHex(hexMessage.toCharArray());
        
        if(type.equals("AES")) {
            return decodeAES(data);
        }
        else{
            return decodeCAESAR(data);
        }
    }
    
    /**
     *Encodes a plaintext message
     * @param message the message
     * @return the encrypted message as hex
     * @throws Exception
     */
    public String encodeMessage(String message) throws Exception {
        if(type.equals("AES")) {
            byte[] data = message.getBytes("UTF-8");
            return encodeAES(data);
        }
        else{
            return encodeCAESAR(message);

        }
    }
    
    private String encodeAES(byte[] data) throws Exception {
        aesCipher.init(Cipher.ENCRYPT_MODE, (SecretKey)myKey);
        byte[] byteEncoded = aesCipher.doFinal(data);
        String encodedHex = Hex.encodeHexString(byteEncoded);
        return encodedHex.toUpperCase();
    }
    
    private String decodeAES(byte[] data) throws Exception {
        aesCipher.init(Cipher.DECRYPT_MODE, (SecretKey) myKey);
        byte[] bytesDecoded = aesCipher.doFinal(data);
        Charset UTF8_CHARSET = Charset.forName("UTF-8");
        return new String(bytesDecoded, UTF8_CHARSET);
    }

    private String encodeCAESAR(String message) {
        StringBuilder stringBuilder = new StringBuilder();
        int length = message.length();
        for(int i = 0; i < length; i++) {
            char c = (char)(message.charAt(i) + (int)myKey);
            stringBuilder.append(c);
        }
        String encodedHex = 
                Hex.encodeHexString(stringBuilder.toString().getBytes());
        return encodedHex.toUpperCase();
    }
    
    private String decodeCAESAR(byte[] data) throws Exception{
        String encoded = new String(data, "UTF-8");
        StringBuilder stringBuilder = new StringBuilder();
        int length = encoded.length();
        for(int i = 0; i < length; i++) {
            char c = (char)(encoded.charAt(i) - (int)myKey);
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
}
