package unet.shadowrouter.utils;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtils {

    public static KeyPair generateKeyPair()throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Adjust the key size as needed
        //KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //PublicKey publicKey = keyPair.getPublic();
        return keyPairGenerator.generateKeyPair();
    }

    public static PublicKey decodePublic(byte[] buf)throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buf);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //PublicKey publicKey = keyFactory.generatePublic(aliceKeySpec);
        return keyFactory.generatePublic(keySpec);
    }

    /*
    public static byte[] generateSecret(PrivateKey privateKey, PublicKey publicKey)throws NoSuchAlgorithmException, InvalidKeyException {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        return keyAgreement.generateSecret();
    }
    */

    public static byte[] sign(PrivateKey privateKey, byte[] data)throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(data);
        return sig.sign();
    }

    public static boolean verify(PublicKey publicKey, byte[] signature, byte[] data)throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }
}
