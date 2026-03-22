package com.example.learningApp.controller.payment;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.payment.CreateVnPayRequest;
import com.example.learningApp.dto.response.order.OrderSuccessResponse;
import com.example.learningApp.service.payment.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class VnPayController {

        final VnPayService vnPayService;
        final String frontendUrl;

        @Autowired
        public VnPayController(
                        VnPayService vnPayService,
                        @Value("${app.frontend-url:https://nibojapan.cloud}") String frontendUrl) {
                log.info("🚀 VnPayController initialized with: vnPayService={}, frontendUrl={}", vnPayService,
                                frontendUrl);
                this.vnPayService = vnPayService;
                this.frontendUrl = frontendUrl;
        }

        /* ===================== CREATE ===================== */

        @PostMapping("/vnpay/create")
        public ResponseEntity<ApiResponse<Map<String, Object>>> createPayment(
                        @RequestBody CreateVnPayRequest request) throws Exception {

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

                Map<String, String> params = new HashMap<>();
                request.getParameterMap()
                                .forEach((k, v) -> params.put(k, v[0]));

                OrderSuccessResponse order = vnPayService.handleVnPayReturn(params);

                // Redirect về FE đúng trang Video của nibojapan.cloud
                String redirectUrl = frontendUrl + "/video";

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
                        vnPayService.handleVnPayIpn(params);

                        return ResponseEntity.ok(
                                        Map.of("RspCode", "00", "Message", "Confirm Success"));

                } catch (Exception e) {

                        return ResponseEntity.ok(
                                        Map.of("RspCode", "97", "Message", "Invalid Checksum"));
                }
        }

}
