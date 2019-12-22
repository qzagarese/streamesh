package io.scicast.streamesh.docker.driver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class JobOutputManager {

    private final String outputFilePath;
    private final List<Tailer> registeredTailers = new ArrayList<>();

    public JobOutputManager(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public InputStream requestStream() {
        Tailer tailer = new Tailer(outputFilePath);
        InputStream stream = tailer.tail();
        registeredTailers.add(tailer);
        return stream;
    }

    public void notifyTermination() {
        registeredTailers.forEach(tailer -> tailer.stop());
    }

    class Tailer {

        private String filePath;
        private long fileLength;
        private long lastKnownPosition;
        private PipedOutputStream pos;
        private Timer timer;
        private TimerTask tailTask;

        Tailer(String filePath) {
            this.filePath = filePath;
        }

        void stop() {
            if (tailTask != null) {
                tailTask.cancel();
            }
            try {
                pos.flush();
                pos.close();
            } catch (IOException e) {
                throw new RuntimeException("Could not close stream for file " + filePath, e);
            }
        }

        InputStream tail() {
            pos = new PipedOutputStream();
            PipedInputStream pis;
            try {
                pis = new PipedInputStream(pos);
            } catch (IOException e) {
                throw new RuntimeException("Could not create input stream for file " + filePath, e);
            }
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
                            while ((b = randomAccessFile.read()) != -1) {
                                pos.write(b);
                            }
                            lastKnownPosition = randomAccessFile.getFilePointer();
                        }
                        randomAccessFile.close();
                        timer.cancel();
                        timer.schedule(this, 1000);
                    } catch (Exception e) {
                        throw new RuntimeException("An error occurred while tailing " + filePath, e);
                    }
                }
            };
            timer = new Timer("Tailer for file " + filePath);
            timer.schedule(tailTask, 0);
            return pis;
        }

    }

}
