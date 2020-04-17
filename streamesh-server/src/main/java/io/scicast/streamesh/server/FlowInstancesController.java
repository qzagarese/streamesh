package io.scicast.streamesh.server;

import io.scicast.streamesh.core.StreameshOrchestrator;
import io.scicast.streamesh.core.exception.NotFoundException;
import io.scicast.streamesh.core.flow.FlowInstance;
import io.scicast.streamesh.core.flow.FlowOutput;
import io.scicast.streamesh.core.flow.execution.FlowOutputRuntimeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
public class FlowInstancesController {

    private static final String TASKS_PATH = "/tasks/";
    @Autowired
    private StreameshOrchestrator orchestrator;

    @Autowired
    private TasksController tasksController;

    @Value("${server.servlet.context-path}")
    private String apiPath;

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

    @GetMapping("/flow-instances/{flowInstanceId}")
    public ResponseEntity<FlowInstance> getFlowInstance(@PathVariable("flowInstanceId")  String flowInstanceId) {
        return ResponseEntity.ok(orchestrator.getFlowInstance(flowInstanceId));
    }

    @GetMapping(value = "/flow-instances/{flowInstanceId}/{outputName}")
    public void getOutput(@PathVariable("flowInstanceId") String flowInstanceId,
                                  @PathVariable("outputName") String outputName,
                                  HttpServletResponse response) throws IOException {
        FlowInstance instance = orchestrator.getFlowInstance(flowInstanceId);
        FlowOutputRuntimeNode outputNode = instance.getExecutionGraph().getOutputNodes().stream()
                .filter(node -> outputName.equals(((FlowOutput) node.getStaticGraphNode().getValue()).getName()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Cannot find output %s for the specified flow.", outputName)));

        if (outputNode.getValue() != null) {
            String value = outputNode.getValue().getParts().stream()
                    .findFirst()
                    .map(part -> part.getValue())
                    .orElse(null);
            if (value != null) {
                value = value.substring(value.indexOf(TASKS_PATH) + TASKS_PATH.length());
                String[] parameters = value.split("/");
                if (parameters.length == 2) {
                    tasksController.getOutput(parameters[0], parameters[1], response);
                }

            }
        }

    }


}
