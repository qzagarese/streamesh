package io.scicast.streamesh.server;

import io.scicast.streamesh.core.JobDescriptor;
import io.scicast.streamesh.core.StreameshOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class JobsController {

    @Autowired
    private StreameshOrchestrator orchestrator;


    @PostMapping(value = "/jobs/{definitionId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<?, ?>> postJob(@PathVariable("definitionId") String definitionId, @RequestBody Map<?, ?> jobInput) {
        JobDescriptor jobDescriptor = orchestrator.scheduleJob(definitionId, jobInput);
        Map<Object, Object> result = new HashMap<>();
        result.put("jobId", jobDescriptor.getId());
        return ResponseEntity.ok(result);
    }


}
