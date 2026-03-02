package com.example.mini_game.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentResponse {
    private String txnRef;
    private String paymentUrl;
}