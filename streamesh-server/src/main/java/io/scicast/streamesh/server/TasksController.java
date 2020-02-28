package io.scicast.streamesh.server;

import io.scicast.streamesh.core.TaskDescriptor;
import io.scicast.streamesh.core.StreameshOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
public class TasksController {

    public static final String STREAMESH_PUBLIC_KEY = "streamesh-public-key";

    @Autowired
    private StreameshOrchestrator orchestrator;

    private Logger logger = Logger.getLogger(getClass().getName());

    @PostMapping(value = "/definitions/{definitionId}/tasks", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<?, ?>> postJob(@PathVariable("definitionId") String definitionId, @RequestBody Map<?, ?> taskInput) {
        String publicKey = (String) taskInput.get(STREAMESH_PUBLIC_KEY);
        TaskDescriptor descriptor;
        if (publicKey == null) {
            descriptor = orchestrator.scheduleTask(definitionId, taskInput);
        } else {
            descriptor = orchestrator.scheduleSecureTask(definitionId, taskInput, publicKey);
        }
        Map<Object, Object> result = new HashMap<>();
        result.put("taskId", descriptor.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/definitions/{definitionId}/tasks")
    public ResponseEntity<Collection<TaskDescriptor>> getTasksByDefinitionId(@PathVariable("definitionId") String definitionId) {
        return ResponseEntity.ok(orchestrator.getTasksByDefinition(definitionId));
    }

    @GetMapping(value = "/tasks/{taskId}/{outputName}")
    public void getOutput(@PathVariable("taskId") String taskId,
                          @PathVariable("outputName") String outputName,
                          HttpServletResponse response) throws IOException {
        InputStream is = orchestrator.getTaskOutput(taskId, outputName);
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
                logger.info(String.format("Output request for job id %s has been cancelled by the client.", taskId)  );
            }
        }
        os.close();
        response.flushBuffer();
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<TaskDescriptor> getJobDetails(@PathVariable("jobId") String jobId) {
        return ResponseEntity.ok(orchestrator.getTask(jobId));
    }

    @GetMapping("/jobs")
    public ResponseEntity<Collection<TaskDescriptor>> getAllJobs() {
        return ResponseEntity.ok(orchestrator.getAllTasks());
    }

}
