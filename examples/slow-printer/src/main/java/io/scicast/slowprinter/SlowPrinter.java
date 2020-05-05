package io.scicast.slowprinter;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SlowPrinter {

    public static void main(String[] args) throws IOException, InterruptedException {
        int howMany = 0;
        AtomicInteger millisBetweenNumbers = new AtomicInteger();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--howMany")) {
                howMany = Integer.parseInt(args[i + 1]);
            } else if (args[i].equals("--millisBetweenNumbers")) {
                millisBetweenNumbers.set(Integer.parseInt(args[i + 1]));
            }
        }


        BufferedWriter bw = new BufferedWriter( new FileWriter("/tmp/output.txt"));

        IntStream.range(0, howMany).forEachOrdered(i -> {
            try {
                Thread.sleep(millisBetweenNumbers.get());
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
