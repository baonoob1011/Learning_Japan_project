package com.example.learningApp.service.payment;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.order.OrderSuccessResponse;
import com.example.learningApp.enums.ProductType;
import com.example.learningApp.service.order.OrderService;
import com.example.learningApp.service.vipPackage.VipPurchaseService;
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
    private final OrderService orderService;
    private final EntityFinder finder;

    /*
     ============================================
     ============== CREATE PAYMENT ==============
     ============================================
     */
    @Transactional
    public Map<String, Object> createPaymentRequest(
            String productId,
            ProductType productType
    ) throws Exception {

        var user = finder.userById();

        Long amount;
        String orderInfo;

        if (productType == ProductType.VIP_PACKAGE) {

            var vipPackage = finder.vipPackageById(productId);
            amount = vipPackage.getPrice();
            orderInfo = "Thanh toan goi VIP " + vipPackage.getName();

        } else if (productType == ProductType.COURSE) {

            var course = finder.courseById(productId);
            amount = course.getPrice();
            orderInfo = "Thanh toan khoa hoc " + course.getTitle();

        } else {
            throw new RuntimeException("Invalid product type");
        }

        String vnp_TxnRef = user.getId() + "_" + UUID.randomUUID();

        var order = orderService.createPendingOrder(
                productId,
                productType,
                vnp_TxnRef,
                amount
        );

        Map<String, String> vnp_Params = new HashMap<>();

        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        String vnp_CreateDate =
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
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

        String vnp_SecureHash =
                hmacSHA512(vnp_HashSecret, hashData.toString());

        String paymentUrl =
                vnp_PayUrl + "?" + query + "&vnp_SecureHash=" + vnp_SecureHash;

        return Map.of(
                "paymentUrl", paymentUrl,
                "orderId", order.getId(),
                "gateway", "VNPAY"
        );
    }

    /*
     ============================================
     ============== HANDLE RETURN ===============
     ============================================
     */
    @Transactional
    public OrderSuccessResponse handleVnPayReturn(Map<String, String> vnpResponse) {
        log.info("🔥 RETURN CALLED - FULL PARAMS: {}", vnpResponse);

        String txnRef = vnpResponse.get("vnp_TxnRef");
        String responseCode = vnpResponse.get("vnp_ResponseCode");
        String transactionNo = vnpResponse.get("vnp_TransactionNo");
        String receivedSecureHash = vnpResponse.get("vnp_SecureHash");

        if (!validateSecureHash(vnpResponse, receivedSecureHash)) {
            log.error("Secure Hash validation FAILED for txnRef: {}", txnRef);
            orderService.markOrderFailed(txnRef);
            throw new RuntimeException("Invalid secure hash");
        }

        if (!"00".equals(responseCode)) {
            orderService.markOrderFailed(txnRef);
            throw new RuntimeException("Payment failed");
        }

        // ✅ Update order SUCCESS
        OrderSuccessResponse response =
                orderService.markOrderSuccess(txnRef, transactionNo);

        return response;
    }

    @Transactional
    public void handleVnPayIpn(Map<String, String> vnpResponse) {

        String txnRef = vnpResponse.get("vnp_TxnRef");
        String responseCode = vnpResponse.get("vnp_ResponseCode");
        String transactionNo = vnpResponse.get("vnp_TransactionNo");
        String receivedSecureHash = vnpResponse.get("vnp_SecureHash");

        if (!validateSecureHash(vnpResponse, receivedSecureHash)) {
            throw new RuntimeException("Invalid checksum");
        }

        if ("00".equals(responseCode)) {
            orderService.markOrderSuccess(txnRef, transactionNo);
        } else {
            orderService.markOrderFailed(txnRef);
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
