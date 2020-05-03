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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@RestController
public class FlowInstancesController {

    private static final String TASKS_PATH = "/tasks/";
    @Autowired
    private StreameshOrchestrator orchestrator;

    @Autowired
    private TasksController tasksController;

    @Value("${server.servlet.context-path}")
    private String apiPath;

    private Logger logger = Logger.getLogger(getClass().getName());

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
        InputStream is = orchestrator.getFlowOutput(flowInstanceId, outputName);
        ServletOutputStream os = response.getOutputStream();
        byte[] buf = new byte[100 * 1024];
        int b = is.read(buf);
        while(b != -1) {
            os.write(buf, 0, b);
            try {
                os.flush();
                b = is.read(buf);
            } catch (IOException e) {
                os.close();
                b = -1;
                logger.info(String.format("Output request for flow id %s has been cancelled by the client.", flowInstanceId));
            }
        }
        os.close();
        response.flushBuffer();

    }


}
