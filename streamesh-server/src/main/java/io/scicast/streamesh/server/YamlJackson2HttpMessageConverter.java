package io.scicast.streamesh.server;


import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

final class YamlJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
    YamlJackson2HttpMessageConverter() {
        super(new YAMLMapper(), MediaType.parseMediaType("application/x-yaml"));
    }
}