package com.example.learningApp.controller.payment;

import com.example.learningApp.service.payment.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class VnPayController {

    private final VnPayService vnPayService;

    /*
     =====================================
     ============== CREATE ===============
     =====================================
     */
    @PostMapping("/vnpay/create")
    public ResponseEntity<Map<String, Object>> createPayment(
            @RequestParam String amount
    ) {

        String orderId = String.valueOf(System.currentTimeMillis());

        Map<String, Object> response =
                vnPayService.createPaymentRequest(amount, orderId);

        return ResponseEntity.ok(response);
    }

    /*
     =====================================
     ===== RETURN (User Redirect) ========
     =====================================
     */
    @GetMapping("/vnpay/return")
    public ResponseEntity<Map<String, String>> paymentReturn(
            HttpServletRequest request
    ) {

        Map<String, String> params = new HashMap<>();
        request.getParameterMap()
                .forEach((k, v) -> params.put(k, v[0]));

        boolean success = vnPayService.handleVnPayReturn(params);

        if (!success) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Thanh toan that bai hoac sai checksum")
            );
        }

        return ResponseEntity.ok(
                Map.of("message", "Thanh toan thanh cong")
        );
    }

    /*
     =====================================
     ============== IPN ==================
     =====================================
     */
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> paymentIPN(
            HttpServletRequest request
    ) {

        Map<String, String> params = new HashMap<>();
        request.getParameterMap()
                .forEach((k, v) -> params.put(k, v[0]));

        boolean success = vnPayService.handleVnPayReturn(params);

        if (!success) {
            return ResponseEntity.ok(
                    Map.of("RspCode", "97", "Message", "Invalid Checksum")
            );
        }

        return ResponseEntity.ok(
                Map.of("RspCode", "00", "Message", "Confirm Success")
        );
    }
}
