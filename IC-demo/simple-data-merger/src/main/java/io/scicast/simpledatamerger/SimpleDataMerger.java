package io.scicast.simpledatamerger;

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

@SpringBootApplication
public class SimpleDataMerger {

    public static void main(String[] args) throws IOException {
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIAW6BDIW7OTA2ZAJGG",
                "GvHJDIxp7Zy1d2F216LUfWBwCo+2S/YXWw+DX9YF"
        );

        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.EU_WEST_2)
                .build();

        S3Object s3object = s3client.getObject("ic-demo-streamesh", "data/manhattan.csv");
        S3ObjectInputStream inputStream = s3object.getObjectContent();

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        BufferedWriter bw = new BufferedWriter(new FileWriter("/tmp/data.csv"));

        String line = br.readLine();
        while(line != null) {
            bw.write(line);
            bw.newLine();
            line = br.readLine();
        }
        br.close();
        bw.flush();
        bw.close();
    }

}
