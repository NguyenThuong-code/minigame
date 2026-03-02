package com.example.mini_game.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "vnpay")
public class VnpayProperties {
    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;
    private String ipnUrl;
    private String orderType;
    private String locale;
    private String currCode;

    private int buyTurnsAmount;
    private long buyTurnsPriceVnd;
}