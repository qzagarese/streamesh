package io.scicast.streamesh.shell.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.scicast.streamesh.shell.Constants;
import io.scicast.streamesh.shell.dto.Definition;
import io.scicast.streamesh.shell.util.TableFactory;
import io.scicast.streamesh.shell.web.RestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.Table;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@ShellComponent
public class DeploymentCommands {

    public static final String ERROR_STATUS_MSG = "Oops! Something went wrong while retrieving service details. Server returned status code ";
    public static final String GENERIC_ERROR_MSG = "An error occurred while contacting the Streamesh Server.";
    private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @ShellMethod(value = "Sends a new service definition to Streamesh Server", key = "apply")
    public String applyDefinition(@ShellOption(value = "-d") String definitionPath) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(definitionPath))));
        String definitionBody = reader.lines().collect(Collectors.joining("\n"));
        RestClient client = new RestClient(System.getProperty(Constants.SERVER_URL_PROPERTY, Constants.SERVER_URL_DEFAULT));
        ResponseEntity<String> stringResponseEntity = client.postYaml("/definitions", definitionBody);

        return "";
    }

    @ShellMethod(value = "Lists the currently deployed services.", key = "get-services")
    public Table getServices() throws JsonProcessingException {
        RestClient client = new RestClient(System.getProperty(Constants.SERVER_URL_PROPERTY, Constants.SERVER_URL_DEFAULT));
        ResponseEntity<String> definitionsResp = client.getJson("/definitions");
        if(!definitionsResp.getStatusCode().equals(HttpStatus.OK)) {
            throw new RuntimeException("Oops! Something went wrong while retrieving the services list.");
        }

        List<Definition> definitions = mapper.readerFor(new TypeReference<List<Definition>>() {
        }).readValue(definitionsResp.getBody());

        String[] headers = {"id", "name", "image", "max concurrent jobs"};
        String[][] data = new String[definitions.size()][headers.length];

        for (int i = 0; i < definitions.size(); i++) {
            data[i][0] = String.format(" %s ", definitions.get(i).getId());
            data[i][1] = String.format(" %s ", definitions.get(i).getName());
            data[i][2] = String.format(" %s ", definitions.get(i).getImage());
            data[i][3] = String.format(" %s ", definitions.get(i).getMaxConcurrentJobs());
        }

        return TableFactory.createTable(headers, data);
    }


    @ShellMethod(value = "Provides the details for the specified service", key = "describe-svc")
    public Table describeService(@ShellOption(value = "--name") String name) throws JsonProcessingException {
        RestClient client = new RestClient(System.getProperty(Constants.SERVER_URL_PROPERTY, Constants.SERVER_URL_DEFAULT))
                .onClientError(ce -> {
                    if (ce.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        System.err.println("Could not find service named " + name);
                    } else {
                        System.err.println(ERROR_STATUS_MSG + ce.getStatusCode());
                    }
                }).onServerError(se -> System.err.println(ERROR_STATUS_MSG + se.getStatusCode()))
                .onGenericError(e -> System.err.println(GENERIC_ERROR_MSG));
        ResponseEntity<String> definitionsResp = client.getJson("/definitions/by-name/" + name);

        if(definitionsResp == null) {
            return null;
        }

        Definition definition = mapper.readerFor(Definition.class).readValue(definitionsResp.getBody());
        String[][] mainData = new String[4][2];
        mainData[0][0] = " Id: ";
        mainData[0][1] = String.format(" %s ", definition.getId());
        mainData[1][0] = " Name: ";
        mainData[1][1] = String.format(" %s ", definition.getName());
        mainData[2][0] = " Image: ";
        mainData[2][1] = String.format(" %s ", definition.getImage());
        mainData[3][0] = " Max concurrent jobs: ";
        mainData[3][1] = String.format(" %s ", definition.getMaxConcurrentJobs());

        return TableFactory.createTable(mainData);
    }



}
