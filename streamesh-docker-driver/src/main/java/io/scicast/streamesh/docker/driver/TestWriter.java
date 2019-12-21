package io.scicast.streamesh.docker.driver;

import java.io.FileWriter;
import java.io.IOException;

public class TestWriter {

    private static long interval = 2000;

    public static void main(String[] args) throws Exception {

        Runnable writer = () -> {
            try {
                writeRandom(args[0]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        writer.run();

    }





    private static void writeRandom(String file) throws IOException, InterruptedException {
        while (true) {
            FileWriter fw = new FileWriter(file, true);
            for (int i = 0; i < 10; i++) {
                fw.write(System.currentTimeMillis() + "\n");
            }
            fw.close();
            Thread.sleep(interval);
        }
    }


}
