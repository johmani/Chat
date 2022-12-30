package test;

import security.Hyper;

import java.security.KeyPair;

public class TestKeys
{
    public static void main(String[] args) throws Exception {

        KeyPair keypair =  Hyper.generateKeyPair();


        System.out.println("The Public Key is: " + keypair.getPublic().getEncoded());
        System.out.println("The Private Key is: " + keypair.getPrivate().getEncoded());

    }
}
