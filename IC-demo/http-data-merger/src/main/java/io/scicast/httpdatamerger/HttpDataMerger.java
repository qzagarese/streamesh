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
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Stream;

@SpringBootApplication
public class HttpDataMerger {

    private static final String HEADER = "id,name,host_id,host_name,neighbourhood_group,neighbourhood," +
            "latitude,longitude,room_type,price,minimum_nights,number_of_reviews,last_review,reviews_per_month," +
            "calculated_host_listings_count,availability_365";

    private static RestTemplate rest = new RestTemplate();

    public static void main(String[] args) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter("/tmp/data.csv"));
        bw.write(HEADER);
        bw.newLine();
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
            InputStream inputStream = download(arg);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));


            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isBlank() && !line.startsWith("id")) {
                    bw.write(line);
                    bw.newLine();
                }
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static InputStream download(String arg) throws IOException {
        return rest.execute(arg, HttpMethod.GET, req -> {
        }, resp -> {
            String pathname = System.currentTimeMillis() + "";
            FileOutputStream fos = new FileOutputStream(new File(pathname));
            StreamUtils.copy(resp.getBody(), fos);
            return new FileInputStream(pathname);
        }, new Object[0]);
    }

}
