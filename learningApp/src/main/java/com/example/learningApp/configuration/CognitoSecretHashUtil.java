package com.example.learningApp.configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CognitoSecretHashUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public static String calculateSecretHash(String username, String clientId, String clientSecret) {
        try {
            if (clientSecret == null || clientSecret.isBlank()) return null;

            String message = username + clientId;
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(clientSecret.getBytes("UTF-8"), HMAC_SHA256));
            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating secret hash", e);
        }
    }
}

