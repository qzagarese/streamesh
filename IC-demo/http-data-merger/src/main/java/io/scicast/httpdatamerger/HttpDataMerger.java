package io.scicast.httpdatamerger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Stream;

@SpringBootApplication
public class HttpDataMerger {

    public static void main(String[] args) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter("/tmp/data.csv"));
        Stream.of(args).forEach(arg -> {
            if (!arg.equalsIgnoreCase("--url")) {
                writeURLContent(arg, bw);
            }
        });
        bw.flush();
        bw.close();
    }

    private static void writeURLContent(String arg, BufferedWriter bw) {
        try {
            URL url = new URL(arg);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
