package io.scicast.streamesh.shell.commands;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class ShellCryptoUtil {

//    static {
//        Security.addProvider(new BouncyCastleProvider());
//    }

    public static final String RSA_ECB_PKCS_1_PADDING = "RSA/ECB/PKCS1Padding";
    public static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    public static final int AES_KEY_SIZE = 256;
    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;


    public static KeyPair getRSAKeyPair(int keyLength) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keyLength);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Could not generate keypair.", e);
        }
    }


    public static PublicKey readPublicKey(String publicKey) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        FileInputStream stream = new FileInputStream(new File(publicKey));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(new X509EncodedKeySpec(stream.readAllBytes()));
    }

    public static byte[] rsaDecrypt(File privateKeyFile, byte[] input) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        FileInputStream stream = new FileInputStream(privateKeyFile);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(stream.readAllBytes()));
        try {
            Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS_1_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedKey = cipher.doFinal(input);
            return decryptedKey;
        } catch (Exception e) {
            throw  new RuntimeException("Could not unwrap AES secret key.", e);
        }
    }

    public static void decryptFile(File input, File output, byte[] decryptedKey, byte[] ivBytes) throws IOException {
        InputStream decryptingStream = getCipherInputStream(new FileInputStream(input), decryptedKey, ivBytes);
        FileOutputStream fos = new FileOutputStream(output);

        byte[] buffer = new byte[100 * 1024];
        int read = decryptingStream.read(buffer);
        while (read != -1) {
            fos.write(buffer);
            read = decryptingStream.read(buffer);
        }
        fos.flush();
        fos.close();

    }

    public static InputStream getCipherInputStream(InputStream stream, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            return new CipherInputStream(stream, cipher);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialise encryption cipher.", e);
        }
    }

}
