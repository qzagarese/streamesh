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

    @GetMapping(value = "/jobs/{jobId}/output")
    public void getOutput(@PathVariable("jobId") String jobId, HttpServletResponse response) throws IOException {
        InputStream is = orchestrator.getJobOutput(jobId);
        OutputStream os = response.getOutputStream();
        int b = is.read();
        while(b != -1) {
            os.write(b);
            b = is.read();
        }
        os.close();
        response.flushBuffer();
    }


}
