package io.scicast.streamesh.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.shell.jline.PromptProvider;

@SpringBootApplication
@ComponentScan("io.scicast.streamesh.shell.commands")
public class StreameshShell {



    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(StreameshShell.class, args);
    }

    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("streamesh-shell:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }
}


