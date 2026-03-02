package com.example.mini_game.util;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class VnpayUtil {
    private VnpayUtil() {}

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC SHA512 error", e);
        }
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.US_ASCII);
    }

    public static String buildHashData(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (String k : keys) {
            String v = params.get(k);
            if (v == null || v.isBlank()) continue;
            if (sb.length() > 0) sb.append("&");
            sb.append(enc(k)).append("=").append(enc(v));
        }
        return sb.toString();
    }

    public static String buildQueryString(Map<String, String> params) {
        return buildHashData(params);
    }

    public static boolean verifySecureHash(Map<String, String> allParams, String hashSecret) {
        String received = allParams.get("vnp_SecureHash");
        if (received == null || received.isBlank()) return false;

        Map<String, String> toSign = new HashMap<>(allParams);
        toSign.remove("vnp_SecureHash");
        toSign.remove("vnp_SecureHashType");

        String data = buildHashData(toSign);
        String calc = hmacSHA512(hashSecret, data);
        return calc.equalsIgnoreCase(received);
    }
}