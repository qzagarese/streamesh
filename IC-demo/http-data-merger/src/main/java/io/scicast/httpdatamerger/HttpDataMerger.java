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
import java.util.stream.Stream;

@SpringBootApplication
public class HttpDataMerger {

    public static void main(String[] args) throws IOException {
        Stream.of(args).forEach(System.out::println);
    }

}
