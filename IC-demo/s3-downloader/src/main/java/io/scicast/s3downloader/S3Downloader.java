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
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIAW6BDIW7OTA2ZAJGG",
                "GvHJDIxp7Zy1d2F216LUfWBwCo+2S/YXWw+DX9YF"
        );

        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.EU_WEST_2)
                .build();

        S3Object s3object = s3client.getObject(args[1], args[3]);
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
