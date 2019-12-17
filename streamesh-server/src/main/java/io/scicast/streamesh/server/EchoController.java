package io.scicast.streamesh.server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EchoController {

    @GetMapping("/echo")
    public ResponseEntity<Void> echo() {
        return ResponseEntity.ok(null);
    }

}
