package io.scicast.streamesh.docker.driver;

import java.io.*;

public class TestReader {


    private static long fileLength;
    private static long lastKnownPosition;
    private static long interval = 2000;


    public static void main(String[] args) {
        Runnable reader = () -> {
            try {
                tailFile(args[0]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        reader.run();
    }

    private static void tailFile(String fileToTail) throws IOException, InterruptedException {
        while (true) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileToTail, "r");
            fileLength = randomAccessFile.length();
            if (fileLength < lastKnownPosition) {
                lastKnownPosition = 0;
            }
            if (fileLength > lastKnownPosition) {
                randomAccessFile.seek(lastKnownPosition);
                String line = null;
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileToTail + "_copy", true));
                while ((line = randomAccessFile.readLine()) != null) {
                    bw.write(line);
                    bw.newLine();
                }
                lastKnownPosition = randomAccessFile.getFilePointer();
                bw.close();
            }
            randomAccessFile.close();
            Thread.sleep(interval);
        }
    }
}
