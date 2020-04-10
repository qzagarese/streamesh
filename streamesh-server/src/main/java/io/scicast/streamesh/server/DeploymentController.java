package io.scicast.streamesh.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.scicast.streamesh.core.Definition;
import io.scicast.streamesh.core.flow.FlowDefinition;
import io.scicast.streamesh.core.MicroPipe;
import io.scicast.streamesh.core.StreameshOrchestrator;
import io.scicast.streamesh.core.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class DeploymentController {

    public static final String MICROPIPE_TYPE = "micropipe";
    private static final String FLOW_TYPE = "flow";
    @Autowired
    private StreameshOrchestrator orchestrator;

    @Autowired
    private ObjectMapper mapper;

    @PostMapping(value = "/definitions", consumes = "application/x-yaml", produces = "application/json")
    public ResponseEntity<Map<?, ?>> applyDefinition(@RequestBody Map<?, Object> definitionMap) {
        String type = (String) definitionMap.getOrDefault("type", MICROPIPE_TYPE);
        Definition definition;
        if (type.equals(MICROPIPE_TYPE)) {
            definition = mapper.convertValue(definitionMap, MicroPipe.class);
        } else if (type.equals(FLOW_TYPE)) {
            definition = mapper.convertValue(definitionMap, FlowDefinition.class);
        } else {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Unrecognized definition type " + type);
        }
        String definitionId = orchestrator.applyDefinition(definition);

        HashMap<Object, Object> result = new HashMap<>();
        result.put("definitionId", definitionId);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/definitions", produces = "application/json")
    public ResponseEntity<Set<Definition>> getDefinitions(@RequestParam(value = "type", required = false) String type) {

        return ResponseEntity.ok(orchestrator.getDefinitions().stream()
            .map(definition -> {
                if (type == null || type.isBlank()) {
                    return definition;
                } else {
                    return type.equals(definition.getType()) ? definition : null;
                }
            }).filter(definition -> definition != null)
                .collect(Collectors.toSet()));
    }

    @GetMapping(value = "/definitions/{id}", produces = "application/json")
    public ResponseEntity<Definition> getDefinitionById(@PathVariable String id) {
        return ResponseEntity.ok(orchestrator.getDefinition(id));
    }

    @GetMapping(value = "/definitions/by-name/{name}", produces = "application/json")
    public ResponseEntity<Definition> getDefinitionByName(@PathVariable String name) {
        return ResponseEntity.ok(orchestrator.getDefinitionByName(name));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public void handleNotFound() {

    }

}
