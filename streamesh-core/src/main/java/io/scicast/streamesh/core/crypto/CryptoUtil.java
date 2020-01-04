package io.scicast.streamesh.core.crypto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class CryptoUtil {

    public static final String RSA_ECB_PKCS_1_PADDING = "RSA/ECB/PKCS1Padding";
    public static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";

//    static {
//        Security.addProvider(new BouncyCastleProvider());
//    }

    public static final int AES_KEY_SIZE = 256;
    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;

    public static WrappedAesGCMKey createWrappedKey(String publicKey) {
        byte[] byteKey = Base64.getDecoder().decode(publicKey.getBytes());
        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
        PublicKey pub;
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            pub = kf.generatePublic(X509publicKey);
        } catch (Exception e) {
            throw new RuntimeException("Could not parse the provided public key.", e);
        }
        SecretKey key = generateRandomSecret();
        byte[] wrappedKey = rsaEncrypt(pub, key.getEncoded());
        byte[] iv = generateRandomIV();

        return WrappedAesGCMKey.builder()
                .publicKey(pub.getEncoded())
                .unwrappedKey(key.getEncoded())
                .wrappedEncryptionKey(wrappedKey)
                .iv(iv)
                .build();
    }

    private static byte[] generateRandomIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    private static byte[] rsaEncrypt(PublicKey pub, byte[] encoded) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS_1_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, pub);
            return cipher.doFinal(encoded);
        } catch (Exception e) {
            throw  new RuntimeException("Could not wrap AES secret key.", e);
        }
    }

    private static SecretKey generateRandomSecret() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES not available.");
        }
        keyGenerator.init(AES_KEY_SIZE);
        return keyGenerator.generateKey();
    }

    public static byte[] encrypt(byte[] buffer, byte[] aesKey, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
            return cipher.doFinal(buffer);
        } catch (Exception e) {
            throw new RuntimeException("Could not encrypt buffer", e);
        }
    }

    public static InputStream getCipherInputStream(InputStream stream, WrappedAesGCMKey key) {
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            SecretKeySpec keySpec = new SecretKeySpec(key.getUnwrappedKey(), "AES");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, key.getIv());
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
            return new CipherInputStream(stream, cipher);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialise encryption cipher.", e);
        }
    }

    @Builder
    @Getter
    public static class WrappedAesGCMKey {

        private byte[] publicKey;
        private byte[] wrappedEncryptionKey;

        @JsonIgnore
        private byte[] unwrappedKey;
        private byte[] iv;

    }
}
