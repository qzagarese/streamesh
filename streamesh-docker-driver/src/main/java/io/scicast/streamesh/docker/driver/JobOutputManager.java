package io.scicast.streamesh.docker.driver;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class JobOutputManager {

    private final String outputFilePath;
    private final List<Tailer> registeredTailers = new ArrayList<>();
    private boolean jobTerminated = false;

    public JobOutputManager(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public InputStream requestStream() {
        Tailer tailer = new Tailer(outputFilePath, jobTerminated);
        InputStream stream = tailer.tail();
        registeredTailers.add(tailer);
        return stream;
    }

    public void notifyTermination() {
        this.jobTerminated = true;
        registeredTailers.forEach(tailer -> tailer.stop());
    }

    class Tailer {

        private Logger logger = Logger.getLogger(getClass().getName());

        private String filePath;
        private long fileLength;
        private long lastKnownPosition;
        private PipedOutputStream pos;
        private Timer timer;
        private TimerTask tailTask;
        private boolean terminationNotified = false;

        Tailer(String filePath, boolean jobTerminated) {
            this.filePath = filePath;
            if (jobTerminated) {
                stop();
            }
        }

        void stop() {
            this.terminationNotified = true;
        }

        InputStream tail() {
            pos = new PipedOutputStream();
            TailingStream tis = new TailingStream(pos);
            tailTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        RandomAccessFile randomAccessFile = null;
                        randomAccessFile = new RandomAccessFile(filePath, "r");
                        fileLength = randomAccessFile.length();

                        if (fileLength < lastKnownPosition) {
                            lastKnownPosition = 0;
                        }
                        if (fileLength > lastKnownPosition) {
                            randomAccessFile.seek(lastKnownPosition);
                            int b;
                            while (((b = randomAccessFile.read()) != -1) && tis.getRequestedBytes() > 0) {
                                pos.write(b);
                            }
                            lastKnownPosition = randomAccessFile.getFilePointer();
                        }
                        if (terminationNotified) {
                            tis.notifyFinalSize(fileLength);
                            pos.close();
                            timer.cancel();
                        }
                        randomAccessFile.close();
                    } catch (Exception e) {
                        throw new RuntimeException("An error occurred while tailing " + filePath, e);
                    }
                }
            };
            timer = new Timer("Tailer for file " + filePath);
            timer.scheduleAtFixedRate(tailTask, 0, 1000);
            return tis;
        }

    }


    class TailingStream extends InputStream {

        private PipedOutputStream pos;
        private PipedInputStream pis;
        private AtomicLong finalSize = new AtomicLong(0);
        private AtomicLong readBytes = new AtomicLong(0);
        private AtomicLong requestedBytes = new AtomicLong(0);
        private Queue<CompletableFuture<Integer>> waitingReads = new LinkedList<>();

        private Logger logger = Logger.getLogger(getClass().getName());
        private Runnable task;

        TailingStream(PipedOutputStream pos) {
            this.pos = pos;
            try {
                pis = new PipedInputStream(pos);
            } catch (IOException e) {
                throw new RuntimeException("Could not initialise tailing stream.", e);
            }
            init();
        }

        private void init() {
            task = new Runnable() {
                @Override
                public void run() {
                    int b = 0;
                    while (! ((finalSize.get() > 0) && (readBytes.get() >= finalSize.get()) && (b == -1))) {
                        CompletableFuture<Integer> value = waitingReads.poll();
                        if (value != null) {
                            try {
                                b = pis.read();
                                readBytes.incrementAndGet();
//                                logger.info(String.format("%s bytes read. %s bytes requested.", readBytes.get(), requestedBytes.get()));
                                requestedBytes.decrementAndGet();
                                value.complete(b);

                            } catch (IOException e) {
                                if (finalSize.get() > 0 && readBytes.get() >= finalSize.get()) {
                                    b = -1;
                                    value.complete(b);
                                } else {
                                    logger.info("Data not yet available. Retrying soon...");
                                }
                            }
                        }
                    }
                    logger.info(String.format("%s bytes read.", readBytes.get()));
                }
            };
            ExecutorService svc = Executors.newSingleThreadExecutor();
            Future<?> future = svc.submit(task);
//            try {
//                future.get();
//            } catch (Exception e) {
//                svc.shutdown();
//            } finally {
//                svc.shutdown();
//            }
        }

        synchronized void notifyFinalSize(long size) {
            this.finalSize.set(size);
        }

        synchronized long getRequestedBytes() {
            return requestedBytes.get();
        }

        @Override
        public int read() throws IOException {
            if(requestedBytes.get() < 0) {
                requestedBytes.set(0);
            }
            requestedBytes.incrementAndGet();
            CompletableFuture<Integer> value = new CompletableFuture<>();
            waitingReads.offer(value);
            return value.join();
        }
    }

}
