package io.scicast.streamesh.server;

import io.scicast.streamesh.core.StreameshOrchestrator;
import io.scicast.streamesh.core.flow.FlowInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    @GetMapping("/flow-instances")
    public ResponseEntity<Set<FlowInstance>> getFlowInstances() {
        return ResponseEntity.ok(orchestrator.getAllFlowInstances());
    }

}
