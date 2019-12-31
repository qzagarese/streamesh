package io.scicast.streamesh.server;

import io.scicast.streamesh.core.JobDescriptor;
import io.scicast.streamesh.core.StreameshOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class JobsController {

    @Autowired
    private StreameshOrchestrator orchestrator;


    @PostMapping(value = "/definitions/{definitionId}/jobs", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<?, ?>> postJob(@PathVariable("definitionId") String definitionId, @RequestBody Map<?, ?> jobInput) {
        JobDescriptor jobDescriptor = orchestrator.scheduleJob(definitionId, jobInput);
        Map<Object, Object> result = new HashMap<>();
        result.put("jobId", jobDescriptor.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/definitions/{definitionId}/jobs")
    public ResponseEntity<Collection<JobDescriptor>> getJobsByDefinitionId(@PathVariable("definitionId") String definitionId) {
        return ResponseEntity.ok(orchestrator.getJobsByDefinition(definitionId));
    }

    @GetMapping(value = "/jobs/{jobId}/output")
    public void getOutput(@PathVariable("jobId") String jobId, HttpServletResponse response) throws IOException {
        InputStream is = orchestrator.getJobOutput(jobId);
        OutputStream os = response.getOutputStream();
        byte[] buf = new byte[100 * 1024];
        int b = is.read(buf);
        while(b != -1) {
            os.write(buf, 0, b);
            b = is.read(buf);
        }
        os.close();
        response.flushBuffer();
    }


    @GetMapping("/jobs")
    public ResponseEntity<Collection<JobDescriptor>> getAllJobs() {
        return ResponseEntity.ok(orchestrator.getAllJobs());
    }

}
