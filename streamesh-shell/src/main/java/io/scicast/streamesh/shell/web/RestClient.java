package io.scicast.streamesh.shell.web;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.function.Consumer;

public class RestClient {

    private String server;
    private RestTemplate rest;
    private Consumer<HttpClientErrorException> onClientError;
    private Consumer<HttpServerErrorException> onServerError;
    private Consumer<Exception> onGenericError;

    public RestClient(String server) {
        this.server = server;
        this.rest = new RestTemplate();
    }

    public RestClient onClientError(Consumer<HttpClientErrorException> errorHandler) {
        this.onClientError = errorHandler;
        return this;
    }

    public RestClient onServerError(Consumer<HttpServerErrorException> errorHandler) {
        this.onServerError = errorHandler;
        return this;
    }

    public RestClient onGenericError(Consumer<Exception> errorHandler) {
        this.onGenericError = errorHandler;
        return this;
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

    private ResponseEntity<String> execute(String uri, String content, HttpHeaders headers, HttpMethod method) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(content, headers);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = rest.exchange(server + uri, method, requestEntity, String.class);
        } catch (HttpClientErrorException ce) {
            if(this.onClientError != null) {
                this.onClientError.accept(ce);
            } else {
                throw ce;
            }
        } catch (HttpServerErrorException se) {
            if(this.onServerError != null) {
                this.onServerError.accept(se);
            } else {
                throw se;
            }
        } catch (Exception e) {
            if(this.onGenericError != null) {
                this.onGenericError.accept(e);
            } else {
                throw e;
            }
        }
        return responseEntity;
    }

    public void download(String uri, OutputStream fos) {
        try {
            rest.execute(server + uri, HttpMethod.GET,  req -> {}, resp -> {
                StreamUtils.copy(resp.getBody(), fos);
                return fos;
            }, new Object[0]);
        } catch (HttpClientErrorException ce) {
            if(this.onClientError != null) {
                this.onClientError.accept(ce);
            } else {
                throw ce;
            }
        } catch (HttpServerErrorException se) {
            if(this.onServerError != null) {
                this.onServerError.accept(se);
            } else {
                throw se;
            }
        } catch (Exception e) {
            if(this.onGenericError != null) {
                this.onGenericError.accept(e);
            } else {
                throw e;
            }
        }
    }
}