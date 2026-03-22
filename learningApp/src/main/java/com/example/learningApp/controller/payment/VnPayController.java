package com.example.learningApp.controller.payment;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.payment.CreateVnPayRequest;
import com.example.learningApp.dto.response.order.OrderSuccessResponse;
import com.example.learningApp.service.payment.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class VnPayController {

    private static final Logger log = LoggerFactory.getLogger(VnPayController.class);

    @Autowired
    private VnPayService vnPayService;

    @Value("${app.frontend-url:https://nibojapan.cloud}")
    private String frontendUrl;

    /* ===================== CREATE ===================== */

    @PostMapping("/vnpay/create")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPayment(
            @RequestBody CreateVnPayRequest request) throws Exception {

        if (vnPayService == null) {
            log.error("❌ CRITICAL ERROR: vnPayService is NULL in createPayment!");
            throw new RuntimeException("vnPayService is not injected - please check Spring logs for Wiring errors");
        }

        Map<String, Object> response = vnPayService.createPaymentRequest(
                request.getProductId(),
                request.getProductType());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Create VNPAY payment successfully",
                        response));
    }

    /* ===================== RETURN ===================== */
    @GetMapping("/vnpay/return")
    public void paymentReturn(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        log.info("🚀 VNPAY Return endpoint hit at: {}", java.time.LocalDateTime.now());
        
        if (vnPayService == null) {
            log.error("❌ CRITICAL ERROR: vnPayService is NULL in paymentReturn!");
            response.sendError(500, "VnPayService is null - restart the server");
            return;
        }

        Map<String, String> params = new HashMap<>();
        request.getParameterMap()
                .forEach((k, v) -> params.put(k, v[0]));

        vnPayService.handleVnPayReturn(params);

        // Redirect về FE đúng trang Video của nibojapan.cloud
        String redirectUrl = frontendUrl + "/video";
        log.info("✅ Redirecting to: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    /* ===================== IPN ===================== */

    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> paymentIPN(
            HttpServletRequest request) {

        Map<String, String> params = new HashMap<>();
        request.getParameterMap()
                .forEach((k, v) -> params.put(k, v[0]));

        try {
            if (vnPayService == null) {
                return ResponseEntity.status(500).build();
            }
            vnPayService.handleVnPayIpn(params);

            return ResponseEntity.ok(
                    Map.of("RspCode", "00", "Message", "Confirm Success"));

        } catch (Exception e) {

            return ResponseEntity.ok(
                    Map.of("RspCode", "97", "Message", "Invalid Checksum"));
        }
    }

}