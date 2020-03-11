package io.scicast.simpledbreader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class SimpleDBReader {

    public static void main(String[] args) throws IOException {
        ApplicationContext app = SpringApplication.run(SimpleDBReader .class, args);
        DBReader reader = app.getBean(DBReader.class);
        reader.execute();
    }

}
