package io.scicast.streamesh.shell.commands;

import io.scicast.streamesh.shell.Constants;
import io.scicast.streamesh.shell.web.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.RestTemplate;

@ShellComponent
public class EchoCommand {


    @ShellMethod(value = "Checks the connection to the Streamesh Server.", key = {"check-connection"})
    public String echo(@ShellOption(value = "--server", defaultValue = ShellOption.NULL) String server) {
        if(server == null) {
            server = System.getProperty(Constants.SERVER_URL_PROPERTY, Constants.SERVER_URL_DEFAULT);
        } else {
            System.setProperty(Constants.SERVER_URL_PROPERTY, server);
        }
        RestClient client = new RestClient(server);

        ResponseEntity<String> resp;
        try {
            resp = client.getJson("/echo");
        } catch (Exception e) {
            return "Could not connect to server " + server;
        }
        if (resp.getStatusCode().equals(HttpStatus.OK)) {
            return "Success! Server listening at " + server;
        } else {
            return "Oops! Server is not ready.";
        }
    }

}
