package io.scicast.streamesh.server;

import io.scicast.streamesh.core.CallableDefinition;
import io.scicast.streamesh.core.StreameshOrchestrator;
import io.scicast.streamesh.core.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
public class DeploymentController {

    @Autowired
    private StreameshOrchestrator orchestrator;

    @PostMapping(value = "/definitions", consumes = "application/x-yaml", produces = "application/json")
    public ResponseEntity applyDefinition(@RequestBody CallableDefinition definition) {
        String definitionId = orchestrator.applyDefinition(definition);
        return ResponseEntity.ok(definitionId);
    }

    @GetMapping(value = "/definitions", produces = "application/json")
    public ResponseEntity<Set<CallableDefinition>> getDefinitions() {
        return ResponseEntity.ok(orchestrator.getDefinitions());
    }

    @GetMapping(value = "/definitions/{id}", produces = "application/json")
    public ResponseEntity<CallableDefinition> getDefinitionById(@PathVariable String id) {
        return ResponseEntity.ok(orchestrator.getDefinition(id));
    }

    @GetMapping(value = "/definitions/by-name/{name}", produces = "application/json")
    public ResponseEntity<CallableDefinition> getDefinitionByName(@PathVariable String name) {
        return ResponseEntity.ok(orchestrator.getDefinitionByName(name));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public void handleNotFound() {

    }

}
