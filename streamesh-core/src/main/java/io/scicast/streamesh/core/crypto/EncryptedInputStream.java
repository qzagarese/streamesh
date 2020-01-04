package io.scicast.streamesh.core.crypto;

import java.io.IOException;
import java.io.InputStream;

public class EncryptedInputStream extends InputStream {

    private final InputStream stream;
    private final CryptoUtil.WrappedAesGCMKey key;
    private byte[] leftOver;

    public EncryptedInputStream(InputStream stream, CryptoUtil.WrappedAesGCMKey key) {
        this.stream = stream;
        this.key = key;
    }

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        int read = this.read(b);
        return read == -1 ? read : b[0];
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (leftOver != null) {
            return handleLeftOver(b);
        } else {
            byte[] buffer = new byte[b.length];
            int read = stream.read(buffer);
            if (read == -1) {
                return read;
            }
            byte[] encrypted = CryptoUtil.encrypt(buffer, key.getUnwrappedKey(), key.getIv());
            if (b.length >= encrypted.length) {
                System.arraycopy(encrypted, 0, b,0, encrypted.length);
                return encrypted.length;
            } else {
                System.arraycopy(encrypted, 0, b, 0, b.length);
                createLeftOver(encrypted, b.length);
                return b.length;
            }
        }


    }

    private void createLeftOver(byte[] encrypted, int startIndex) {
        leftOver = new byte[encrypted.length - startIndex];
        System.arraycopy(encrypted, startIndex, leftOver, 0, leftOver.length);
    }

    private int handleLeftOver(byte[] b) {
        if (b.length >= leftOver.length) {
            System.arraycopy(leftOver, 0, b, 0, leftOver.length);
            int read = leftOver.length;
            leftOver = null;
            return read;
        } else {
            System.arraycopy(leftOver, 0, b, 0, b.length);
            byte[] newLeftOver = new byte[leftOver.length - b.length];
            System.arraycopy(leftOver, b.length, newLeftOver, 0, newLeftOver.length);
            leftOver = newLeftOver;
            return b.length;
        }
    }
}
