package io.scicast.s3downloader;

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
public class S3Downloader {

    public static void main(String[] args) throws IOException {

        String accessKey = null;
        String secretKey = null;
        String bucket = null;
        String filePath = null;

        for(int i = 0; i < args.length; i++) {
            if (args[i].equals("--accessKey")) {
                accessKey = args[i + 1];
            } else if (args[i].equals("--secretKey")) {
                secretKey = args[i + 1];
            } else if (args[i].equals("--bucket")) {
                bucket = args[i + 1];
            } else if (args[i].equals("--file")) {
                filePath = args[i + 1];
            }
        }


        AWSCredentials credentials = new BasicAWSCredentials(
                accessKey,
                secretKey
        );

        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.EU_WEST_2)
                .build();

        S3Object s3object = s3client.getObject(bucket, filePath);
        S3ObjectInputStream inputStream = s3object.getObjectContent();

        FileOutputStream fos = new FileOutputStream("/tmp/data.csv");

        byte[] buffer = new byte[100 * 1024];
        int b = inputStream.read(buffer);
        while(b != -1) {
            fos.write(buffer, 0, b);
            b = inputStream.read(buffer);
        }
        inputStream.close();
        fos.flush();
        fos.close();
    }

}
