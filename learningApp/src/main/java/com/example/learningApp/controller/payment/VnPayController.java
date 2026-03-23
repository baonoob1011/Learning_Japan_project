package com.example.learningApp.controller.payment;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.payment.CreateVnPayRequest;
import com.example.learningApp.service.payment.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class VnPayController {

    private static final Logger log = LoggerFactory.getLogger(VnPayController.class);

    private final VnPayService vnPayService;

    @Value("${app.frontend-url:https://nibojapan.cloud}")
    private String frontendUrl;

    @PostMapping("/vnpay/create")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPayment(@RequestBody CreateVnPayRequest request) throws Exception {
        Map<String, Object> response = vnPayService.createPaymentRequest(request.getProductId(), request.getProductType());
        return ResponseEntity.ok(ApiResponse.success("Create VNPAY payment successfully", response));
    }

    @PostMapping("/vnpay/retry/{orderCode}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> retryPayment(@PathVariable String orderCode) throws Exception {
        Map<String, Object> response = vnPayService.retryPaymentRequest(orderCode);
        return ResponseEntity.ok(ApiResponse.success("Retry VNPAY payment successfully", response));
    }

    @GetMapping("/vnpay/return")
    public void paymentReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("VNPAY Return endpoint hit");

        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

        vnPayService.handleVnPayReturn(params);

        String redirectUrl = frontendUrl + "/video";
        log.info("Redirecting to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> paymentIPN(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

        try {
            vnPayService.handleVnPayIpn(params);
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("RspCode", "97", "Message", "Invalid Checksum"));
        }
    }
}