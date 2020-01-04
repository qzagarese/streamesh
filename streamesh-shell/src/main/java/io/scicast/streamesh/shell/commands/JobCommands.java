package io.scicast.streamesh.shell.commands;

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

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;

import static io.scicast.streamesh.shell.Constants.ERROR_STATUS_MSG;
import static io.scicast.streamesh.shell.Constants.GENERIC_ERROR_MSG;

@ShellComponent
public class JobCommands {

    public static final String STREAMESH_PUBLIC_KEY = "streamesh-public-key";

    private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @ShellMethod(value = "Schedules a new job for the specified service id.", key = "run-job")
    public String scheduleJob(@ShellOption(value = "--svc-id") String definitionId,
                              @ShellOption(value = "--json-body", defaultValue = ShellOption.NULL) String jsonBody,
                              @ShellOption(value = "--json-file", defaultValue = ShellOption.NULL) String jsonFile,
                              @ShellOption(value = "--public-key", defaultValue = ShellOption.NULL) String publicKey)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        RestClient client = new RestClient(System.getProperty(Constants.SERVER_URL_PROPERTY, Constants.SERVER_URL_DEFAULT))
                .onClientError(ce -> {
                    if (ce.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        System.err.println("Could not find service with id " + definitionId);
                    } else {
                        System.err.println(ERROR_STATUS_MSG + ce.getStatusCode());
                    }
                }).onServerError(se -> System.err.println(ERROR_STATUS_MSG + se.getStatusCode()))
                .onGenericError(e -> System.err.println(GENERIC_ERROR_MSG));
        Map<String, Object> jsonMap = handleJsonBody(jsonBody, jsonFile);
        if (publicKey != null) {
            PublicKey pk = ShellCryptoUtil.readPublicKey(publicKey);
            jsonMap.put(STREAMESH_PUBLIC_KEY, Base64.getEncoder().encodeToString(pk.getEncoded()));
        }

        ResponseEntity<String> jobResponse = client.postJson("/definitions/" + definitionId + "/jobs", mapper.writerFor(new TypeReference<Map<?, ?>>() {
        }).writeValueAsString(jsonMap));

        if (jobResponse == null) {
            return null;
        }
        Map<?, ?> responseBody = mapper.readerFor(new TypeReference<Map<?, ?>>() {
        }).readValue(jobResponse.getBody());

        return "Job scheduled with id: " + responseBody.get("jobId");

    }

    private Map<String, Object> handleJsonBody(@ShellOption(value = "--json-body", defaultValue = ShellOption.NULL) String jsonBody, @ShellOption(value = "--json-file", defaultValue = ShellOption.NULL) String jsonFile) throws IOException {
        if (jsonBody == null && jsonFile != null) {
            jsonBody = readFile(jsonFile);
        }
        if (jsonBody == null) {
            jsonBody = "{}";
        }
        return mapper.readerFor(new TypeReference<Map<?, ?>>() {
        }).readValue(jsonBody);
    }

    private String readFile(String jsonFile) throws IOException {
        FileInputStream fis = new FileInputStream(jsonFile);
        return new String(fis.readAllBytes());
    }


    @ShellMethod(value = "Retrieves the output of a job specified by job-id.", key = "get-result")
    public void getJobOutput(@ShellOption("--job-id") String jobId,
                               @ShellOption("--output-file") String outputFile) throws IOException {
        RestClient client = new RestClient(System.getProperty(Constants.SERVER_URL_PROPERTY, Constants.SERVER_URL_DEFAULT))
                .onClientError(ce -> {
                    if (ce.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        System.err.println("Could not find job with id " + jobId);
                    } else {
                        System.err.println(ERROR_STATUS_MSG + ce.getStatusCode());
                    }
                }).onServerError(se -> System.err.println(ERROR_STATUS_MSG + se.getStatusCode()))
                .onGenericError(e -> System.err.println(GENERIC_ERROR_MSG));
        FileOutputStream fos = new FileOutputStream(new File(outputFile));
        client.download("/jobs/" + jobId + "/output", fos);
        fos.close();
    }

}
