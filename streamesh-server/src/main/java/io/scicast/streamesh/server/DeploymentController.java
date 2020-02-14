package io.scicast.streamesh.server;

import io.scicast.streamesh.core.Micropipe;
import io.scicast.streamesh.core.StreameshOrchestrator;
import io.scicast.streamesh.core.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
public class DeploymentController {

    @Autowired
    private StreameshOrchestrator orchestrator;

    @PostMapping(value = "/definitions", consumes = "application/x-yaml", produces = "application/json")
    public ResponseEntity<Map<?, ?>> applyDefinition(@RequestBody Micropipe definition) {
        String definitionId = orchestrator.applyDefinition(definition);
        HashMap<Object, Object> result = new HashMap<>();
        result.put("definitionId", definitionId);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/definitions", produces = "application/json")
    public ResponseEntity<Set<Micropipe>> getDefinitions() {
        return ResponseEntity.ok(orchestrator.getDefinitions());
    }

    @GetMapping(value = "/definitions/{id}", produces = "application/json")
    public ResponseEntity<Micropipe> getDefinitionById(@PathVariable String id) {
        return ResponseEntity.ok(orchestrator.getDefinition(id));
    }

    @GetMapping(value = "/definitions/by-name/{name}", produces = "application/json")
    public ResponseEntity<Micropipe> getDefinitionByName(@PathVariable String name) {
        return ResponseEntity.ok(orchestrator.getDefinitionByName(name));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public void handleNotFound() {

    }

}
