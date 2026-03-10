package com.aiops.connection.controller;

import com.aiops.connection.dto.PairingRequest;
import com.aiops.connection.dto.PairingResponse;
import com.aiops.connection.service.PairingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pairing")
public class PairingController {

    @Autowired
    private PairingService pairingService;

    @PostMapping("/request")
    public ResponseEntity<PairingResponse> request(@RequestBody PairingRequest request) {
        PairingResponse response = pairingService.processPairingRequest(request);
        
        if ("APPROVED".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<Boolean> verify(@PathVariable String token) {
        boolean valid = pairingService.verifyPairingToken(token);
        return ResponseEntity.ok(valid);
    }
}
