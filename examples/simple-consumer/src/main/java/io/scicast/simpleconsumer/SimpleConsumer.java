package io.scicast.simpleconsumer;

import java.io.*;
import java.net.URL;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SimpleConsumer {

    public static void main(String[] args) throws IOException {
        int multiplier = 2;
        BufferedWriter bw = new BufferedWriter(new FileWriter("/tmp/output.txt"));
        BufferedReader br = new BufferedReader(new InputStreamReader(new URL(args[1]).openConnection().getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            int i = 0;
            try {
                i = Integer.parseInt(line);
                bw.write(i * multiplier + "");
                bw.newLine();
                bw.flush();
                System.out.println(i * multiplier);
            } catch (NumberFormatException e) {
                System.out.println(line + " is not  number. Skipping.");
            }
        }

        bw.flush();
        bw.close();
        br.close();

    }

}
