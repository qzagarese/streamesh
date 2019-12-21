package io.scicast.streamesh.docker.driver;

import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestEOF {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        InputStream fromFile = new FileInputStream("test.txt");
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);


        Runnable writer = () -> {
            try {
                int b = fromFile.read();
                while (b != -1) {
                    pos.write(b);
                    b = fromFile.read();
                }
                pos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };


        Runnable reader = () -> {
            int read = 0;
            try {
                FileOutputStream fos = new FileOutputStream("test_copy.txt");
                int b = pis.read();
                fos.write(b);
                while (b != -1) {
                    b = pis.read();
                    fos.write(b);
                    read++;
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            System.out.println("Read and wrote " + read);
        };

        ExecutorService svc = Executors.newFixedThreadPool(2);
        Future<?> writerFut = svc.submit(writer);
        Future<?> readerFut = svc.submit(reader);

        writerFut.get();
        readerFut.get();
        svc.shutdown();

    }

}
