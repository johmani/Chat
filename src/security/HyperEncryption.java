package security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.security.*;

public class HyperEncryption
{
    private static final String RSA = "RSA";

    public static KeyPair generateKeyPair()throws Exception
    {
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
        keyPairGenerator.initialize(2048, secureRandom);
        return keyPairGenerator.generateKeyPair();
    }

    // Encryption function which converts
    public static byte[] Encrept(String plainText, PublicKey publicKey) throws Exception
    {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plainText.getBytes());
    }

    // Decryption function
    public static String Decrept(String cipherText, PrivateKey privateKey) throws Exception
    {
        byte[] messageToBytes = cipherText.getBytes();
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(messageToBytes);
        return new String(result);
    }

    // Decryption function
    public static String Decrept(byte[] cipherText, PrivateKey privateKey) throws Exception
    {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        byte[] result = cipher.doFinal(cipherText);
        return new String(result);
    }


    public static void main(String args[]) throws Exception
    {
        KeyPair keypair =  HyperEncryption.generateKeyPair();

        System.out.println("The Public Key is: " + DatatypeConverter.printHexBinary(keypair.getPublic().getEncoded()));
        System.out.println("The Private Key is: " + DatatypeConverter.printHexBinary(keypair.getPrivate().getEncoded()));

        SecretKey symmetrickey = SymmetricEncryption.GenerateSessionKey();

        //Encrypt session key
        byte[] EncryptedKey = HyperEncryption.Encrept(DatatypeConverter.printHexBinary(symmetrickey.getEncoded()),keypair.getPublic());

        String str = java.util.Arrays.toString(EncryptedKey);
        System.out.println(str.getBytes());
    }
}
