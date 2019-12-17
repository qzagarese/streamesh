package io.scicast.streamesh.shell.web;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestClient {

    private String server;
    private RestTemplate rest;

    public RestClient(String server) {
        this.server = server;
        this.rest = new RestTemplate();
    }

    public ResponseEntity<String> getJson(String uri) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "*/*");
        return execute(uri, "", headers, HttpMethod.GET);
    }

    public ResponseEntity<String> postJson(String uri, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "*/*");
        headers.add("Content-Type", "application/json");
        return execute(uri, content, headers, HttpMethod.POST);
    }

    public ResponseEntity<String> postYaml(String uri, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "*/*");
        headers.add("Content-Type", "application/x-yaml");
        return execute(uri, content, headers, HttpMethod.POST);
    }

    private ResponseEntity<String> execute(String uri, String content, HttpHeaders headers, HttpMethod post) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(content, headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri, post, requestEntity, String.class);
        return responseEntity;
    }

}