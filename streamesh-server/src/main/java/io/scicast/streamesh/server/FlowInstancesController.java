package io.scicast.streamesh.server;

import io.scicast.streamesh.core.StreameshOrchestrator;
import io.scicast.streamesh.core.flow.FlowInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FlowInstancesController {

    @Autowired
    private StreameshOrchestrator orchestrator;

    @PostMapping("/definitions/{definitionId}/instances")
    public ResponseEntity<Map<String, Object>> postFlowInstance(@PathVariable("definitionId") String definitionId,
                                                                @RequestBody Map<?, ?> input) {
        FlowInstance instance = orchestrator.scheduleFlow(definitionId, input);
        Map<String, Object> result = new HashMap<>();
        result.put("flowInstanceId", instance.getId());
        return ResponseEntity.ok(result);
    }

}
