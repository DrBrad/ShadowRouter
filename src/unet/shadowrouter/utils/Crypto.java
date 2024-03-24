package unet.shadowrouter.utils;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class Crypto {

    public static KeyPair generateKeyPair()throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
        keyPairGenerator.initialize(2048); // Adjust the key size as needed
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //PublicKey publicKey = keyPair.getPublic();
        return keyPairGenerator.generateKeyPair();
    }

    public static PublicKey decodePublic(byte[] buf)throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buf);
        KeyFactory keyFactory = KeyFactory.getInstance("DH");
        //PublicKey publicKey = keyFactory.generatePublic(aliceKeySpec);
        return keyFactory.generatePublic(keySpec);
    }

    public static byte[] generateSecret(PrivateKey privateKey, PublicKey publicKey)throws NoSuchAlgorithmException, InvalidKeyException {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        return keyAgreement.generateSecret();
    }
}
