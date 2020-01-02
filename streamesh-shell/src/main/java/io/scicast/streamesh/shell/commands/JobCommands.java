package io.scicast.streamesh.shell.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.scicast.streamesh.shell.Constants;
import io.scicast.streamesh.shell.web.RestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Map;

import static io.scicast.streamesh.shell.Constants.ERROR_STATUS_MSG;
import static io.scicast.streamesh.shell.Constants.GENERIC_ERROR_MSG;

@ShellComponent
public class JobCommands {

    private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @ShellMethod(value = "Schedules a new job for the specified service id.", key = "run-job")
    public String scheduleJob(@ShellOption(value = "--svc-id") String definitionId,
                              @ShellOption(value = "--json-body", defaultValue = ShellOption.NULL) String jsonBody) throws JsonProcessingException {
        RestClient client = new RestClient(System.getProperty(Constants.SERVER_URL_PROPERTY, Constants.SERVER_URL_DEFAULT))
                .onClientError(ce -> {
                    if (ce.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        System.err.println("Could not find service with id " + definitionId);
                    } else {
                        System.err.println(ERROR_STATUS_MSG + ce.getStatusCode());
                    }
                }).onServerError(se -> System.err.println(ERROR_STATUS_MSG + se.getStatusCode()))
                .onGenericError(e -> System.err.println(GENERIC_ERROR_MSG));
        ResponseEntity<String> jobResponse = client.postJson("/definitions/" + definitionId + "/jobs", jsonBody == null ? "{}" : jsonBody);

        if(jobResponse == null) {
            return null;
        }
        Map<?, ?> responseBody = mapper.readerFor(new TypeReference<Map<?, ?>>() {
        }).readValue(jobResponse.getBody());

        return "Job scheduled with id: " + responseBody.get("jobId");

    }


    @ShellMethod(value = "Retrieves the output of a job specified by job-id.", key = "get-output")
    public String getJobOutput(@ShellOption("--job-id") String jobId) {
        RestClient client = new RestClient(System.getProperty(Constants.SERVER_URL_PROPERTY, Constants.SERVER_URL_DEFAULT))
                .onClientError(ce -> {
                    if (ce.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        System.err.println("Could not find job with id " + jobId);
                    } else {
                        System.err.println(ERROR_STATUS_MSG + ce.getStatusCode());
                    }
                }).onServerError(se -> System.err.println(ERROR_STATUS_MSG + se.getStatusCode()))
                .onGenericError(e -> System.err.println(GENERIC_ERROR_MSG));
        ResponseEntity<String> jobResponse = client.getJson("/jobs/" + jobId + "/output");

        if(jobResponse == null) {
            return null;
        }
        return jobResponse.getBody();
    }

}
