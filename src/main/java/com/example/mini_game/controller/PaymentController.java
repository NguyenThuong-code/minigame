package com.example.mini_game.controller;

import com.example.mini_game.dto.CreatePaymentResponse;
import com.example.mini_game.dto.VnpIpnResponse;
import com.example.mini_game.service.VnpayPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
public class PaymentController {

    private final VnpayPaymentService vnpayPaymentService;

    @PostMapping("/buy-turns")
    public ResponseEntity<CreatePaymentResponse> createBuyTurns(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String keycloakId = jwt.getSubject();
        String ip = request.getRemoteAddr();

        return ResponseEntity.ok(vnpayPaymentService.createBuyTurnsPayment(keycloakId, ip));
    }

    @GetMapping("/return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> params) {
//        System.out.println(params.toString());
        return ResponseEntity.ok("Return received");
    }

    @GetMapping("/ipn")
    public ResponseEntity<VnpIpnResponse> vnpayIpn(@RequestParam Map<String, String> params) {
//        System.out.println("vnpayIpn");
        VnpIpnResponse resp = vnpayPaymentService.handleIpn(params);
        return ResponseEntity.ok(resp);
    }
}