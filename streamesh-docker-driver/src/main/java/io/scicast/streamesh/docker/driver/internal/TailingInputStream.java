package io.scicast.streamesh.docker.driver.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class TailingInputStream extends InputStream {

    private Logger logger = Logger.getLogger(getClass().getName());

    private String filePath;
    private boolean writeComplete;
    private long fileLength;
    private long lastKnownPosition = 0;
    private final int BLOCK_SIZE = 100 * 1024;
    private final int NUMBER_OF_BLOCKS = 20;

    private Runnable producerTask;

    private BlockingQueue<ReadResult> blocks = new LinkedBlockingQueue<>(NUMBER_OF_BLOCKS);
    private int readBytes = 0;

    public TailingInputStream(String filePath) {
        this(filePath, false);
    }

    public TailingInputStream(String filePath, boolean writeComplete) {
        this.filePath = filePath;
        this.writeComplete = writeComplete;
        init();
    }

    private void init() {
        producerTask = new Runnable() {
            @Override
            public void run() {
                byte[] buf = new byte[BLOCK_SIZE];
                RandomAccessFile raf = null;
                int read = 0;
                try {
                    raf = new RandomAccessFile(filePath, "r");
                    fileLength = raf.length();
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Could not locate file %s", filePath));
                }
                while (fileLength > lastKnownPosition || !writeComplete) {
                    try {
                        raf = new RandomAccessFile(filePath, "r");
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(String.format("Could not locate file %s", filePath));
                    }
                    try {
                        fileLength = raf.length();
//                    if (fileLength < lastKnownPosition.get()) {
//                        lastKnownPosition.set(0);
//                    }
                        if (fileLength > lastKnownPosition) {
                            raf.seek(lastKnownPosition);
                            read = raf.read(buf);

                            try {
                                blocks.put(ReadResult.builder()
                                        .buffer(buf)
                                        .readBytes(read)
                                        .build());
                            } catch (InterruptedException e) {
                                logger.severe("Could not add buffer to the queue.");
                            }
                        }
                        lastKnownPosition = raf.getFilePointer();
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        ExecutorService svc = Executors.newSingleThreadExecutor();
        svc.submit(producerTask);
    }

    public synchronized void notifyWriteCompletion() {
        this.writeComplete = true;
    }

    @Override
    public int read() throws IOException {
        byte[] buf = new byte[1];
        int read = read(buf);
        return read != -1 ? (int) buf[0] : read;
    }

    @Override
    public int read(byte[] buf) throws IOException {
        if ((fileLength > 0) && writeComplete && (readBytes >= fileLength)) {
            return -1;
        }

        ReadResult rr = null;
        try {
            rr = blocks.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (buf.length >= rr.getReadBytes()) {
            System.arraycopy(rr.getBuffer(), 0, buf, 0, rr.getBuffer().length);
            readBytes += rr.getReadBytes();
            return rr.getReadBytes();
        } else {
            byte[] remainingBlockBytes = new byte[rr.getBuffer().length - buf.length];
            System.arraycopy(rr.getBuffer(), 0, buf, 0, buf.length);
            System.arraycopy(rr.getBuffer(),buf.length - 1, remainingBlockBytes,0, (rr.getBuffer().length - buf.length));
            rr.setBuffer(remainingBlockBytes);
            rr.setReadBytes(rr.getBuffer().length - buf.length);
            readBytes += buf.length;
            return buf.length;
        }

    }

}
