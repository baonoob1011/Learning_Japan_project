package com.example.learningApp.service.payment;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnPayService {

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.pay-url}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    /*
     ============================================
     ============== CREATE PAYMENT ==============
     ============================================
     */
    @Transactional
    public Map<String, Object> createPaymentRequest(String amountVND, String orderId) {

        try {
            long amount = Long.parseLong(amountVND) * 100; // VNPay requires smallest unit
            String vnp_TxnRef = orderId + "_" + UUID.randomUUID();

            Map<String, String> vnp_Params = new HashMap<>();

            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", "127.0.0.1");

            String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            // Build query & hash
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);

                if (fieldValue != null && !fieldValue.isEmpty()) {

                    hashData.append(fieldName)
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    if (itr.hasNext()) {
                        hashData.append("&");
                        query.append("&");
                    }
                }
            }

            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());

            String paymentUrl = vnp_PayUrl + "?" + query + "&vnp_SecureHash=" + vnp_SecureHash;

            log.info("VNPay Payment URL created: {}", paymentUrl);

            return Map.of(
                    "paymentUrl", paymentUrl,
                    "orderId", orderId,
                    "gateway", "VNPAY"
            );

        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPay payment request: " + e.getMessage(), e);
        }
    }

    /*
     ============================================
     ============== HANDLE RETURN ===============
     ============================================
     */
    @Transactional
    public boolean handleVnPayReturn(Map<String, String> vnpResponse) {

        String txnRef = vnpResponse.get("vnp_TxnRef");
        String responseCode = vnpResponse.get("vnp_ResponseCode");
        String receivedSecureHash = vnpResponse.get("vnp_SecureHash");

        if (!validateSecureHash(vnpResponse, receivedSecureHash)) {
            log.error("Secure Hash validation FAILED for txnRef: {}", txnRef);
            return false;
        }

        if ("00".equals(responseCode)) {
            log.info("Transaction {} completed successfully", txnRef);
            return true;
        } else {
            log.warn("Transaction {} failed with code {}", txnRef, responseCode);
            return false;
        }
    }

    /*
     ============================================
     ============== VALIDATE HASH ===============
     ============================================
     */
    private boolean validateSecureHash(Map<String, String> vnpResponse, String receivedSecureHash) {

        try {
            Map<String, String> data = new HashMap<>(vnpResponse);
            data.remove("vnp_SecureHash");
            data.remove("vnp_SecureHashType");

            List<String> fieldNames = new ArrayList<>(data.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();

            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
                String fieldName = itr.next();
                String fieldValue = data.get(fieldName);

                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName)
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    if (itr.hasNext()) {
                        hashData.append("&");
                    }
                }
            }

            String calculatedSecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());

            return calculatedSecureHash.equalsIgnoreCase(receivedSecureHash);

        } catch (Exception e) {
            log.error("Error validating secure hash: {}", e.getMessage());
            return false;
        }
    }

    /*
     ============================================
     ============== HMAC SHA512 =================
     ============================================
     */
    private String hmacSHA512(String key, String data) throws Exception {

        Mac hmac512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec =
                new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

        hmac512.init(secretKeySpec);

        byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            hash.append(String.format("%02x", b));
        }

        return hash.toString();
    }
}
