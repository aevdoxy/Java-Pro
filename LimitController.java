package com.example.limits.web;

import com.example.limits.model.Limit;
import com.example.limits.service.LimitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/limits")
public class LimitController {

    private final LimitService service;

    public LimitController(LimitService service) { this.service = service; }

    @GetMapping("/{userId}")
    public ResponseEntity<Limit> get(@PathVariable String userId) {
        return ResponseEntity.ok(service.getOrCreate(userId));
    }

    @PostMapping("/{userId}/reserve")
    public ResponseEntity<Map<String,String>> reserve(@PathVariable String userId, @RequestBody Map<String,String> body) {
        BigDecimal amount = new BigDecimal(body.get("amount"));
        String reservationId = service.reserve(userId, amount);
        return ResponseEntity.ok(Map.of("reservationId", reservationId));
    }

    @PostMapping("/reservation/{reservationId}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable String reservationId) {
        service.confirm(reservationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reservation/{reservationId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String reservationId) {
        service.cancel(reservationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/decrease")
    public ResponseEntity<Void> decrease(@PathVariable String userId, @RequestBody Map<String,String> body) {
        BigDecimal amount = new BigDecimal(body.get("amount"));
        service.decreaseDirect(userId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/restore")
    public ResponseEntity<Void> restore(@PathVariable String userId, @RequestBody Map<String,String> body) {
        BigDecimal amount = new BigDecimal(body.get("amount"));
        service.restore(userId, amount);
        return ResponseEntity.ok().build();
    }
}\n