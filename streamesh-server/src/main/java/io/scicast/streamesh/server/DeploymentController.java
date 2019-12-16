package io.scicast.streamesh.server;

import io.scicast.streamesh.core.CallableDefinition;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeploymentController {

    @PostMapping(value = "/definitions", consumes = "application/x-yaml")
    public void applyDefinition(@RequestBody CallableDefinition definition) {

    }


}
