package security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.security.*;

public class HyperEncryption
{
    private static final String RSA = "RSA";

    // Encryption function which converts
    public static String Encrept(String plainText, PublicKey publicKey) throws Exception
    {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return DatatypeConverter.printHexBinary(cipher.doFinal(plainText.getBytes()));
    }

    // Decryption function
    public static String Decrept(String cipherText, PrivateKey privateKey) throws Exception
    {
        byte[] reqByet = DatatypeConverter.parseHexBinary(cipherText);
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        byte[] result = cipher.doFinal(reqByet);
        return new String(result);
    }
}
