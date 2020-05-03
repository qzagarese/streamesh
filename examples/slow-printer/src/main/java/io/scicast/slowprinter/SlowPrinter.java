package io.scicast.slowprinter;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SlowPrinter {

    public static void main(String[] args) throws IOException, InterruptedException {

        Thread.sleep(20000);

        BufferedWriter bw = new BufferedWriter( new FileWriter("/tmp/output.txt"));

        IntStream.range(0, 600).forEachOrdered(i -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                bw.write(i + "");
                bw.newLine();
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        bw.close();
    }

}
