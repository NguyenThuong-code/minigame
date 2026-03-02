package com.example.mini_game.service;
import com.example.mini_game.dto.CreatePaymentResponse;
import com.example.mini_game.dto.PaymentStatus;
import com.example.mini_game.dto.VnpIpnResponse;
import com.example.mini_game.dto.VnpayProperties;
import com.example.mini_game.entity.PaymentTransaction;
import com.example.mini_game.repo.PaymentTransactionRepository;
import com.example.mini_game.repo.UserProfileRepository;
import com.example.mini_game.util.VnpayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VnpayPaymentService {

    private final VnpayProperties props;
    private final PaymentTransactionRepository paymentRepo;
    private final UserProfileRepository userProfileRepository; // repo của bạn

    private static final DateTimeFormatter VNP_TIME =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    @Transactional
    public CreatePaymentResponse createBuyTurnsPayment(String keycloakId, String clientIp) {

        String txnRef = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        long amountVnd = props.getBuyTurnsPriceVnd();

        paymentRepo.save(PaymentTransaction.builder()
                .txnRef(txnRef)
                .keycloakId(keycloakId)
                .amountVnd(amountVnd)
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build());

        Map<String, String> vnp = new HashMap<>();
        vnp.put("vnp_Version", "2.1.0");
        vnp.put("vnp_Command", "pay");
        vnp.put("vnp_TmnCode", props.getTmnCode());
        vnp.put("vnp_Amount", String.valueOf(amountVnd * 100));
        vnp.put("vnp_CurrCode", props.getCurrCode());
        vnp.put("vnp_TxnRef", txnRef);
        vnp.put("vnp_OrderInfo", "Buy " + props.getBuyTurnsAmount() + " turns");
        vnp.put("vnp_OrderType", props.getOrderType());
        vnp.put("vnp_Locale", props.getLocale());
        vnp.put("vnp_ReturnUrl", props.getReturnUrl());
        vnp.put("vnp_CreateDate", VNP_TIME.format(Instant.now()));
        vnp.put("vnp_ExpireDate", VNP_TIME.format(Instant.now().plusSeconds(15 * 60))); // +15 phút

// ép tạm local
        if ("0:0:0:0:0:0:0:1".equals(clientIp) || "::1".equals(clientIp)) {
            clientIp = "127.0.0.1";
        }
        vnp.put("vnp_IpAddr", clientIp);

        String hashData = VnpayUtil.buildHashData(vnp);
        String secureHash = VnpayUtil.hmacSHA512(props.getHashSecret(), hashData);

        String paymentUrl = props.getPayUrl() + "?" + VnpayUtil.buildQueryString(vnp) + "&vnp_SecureHash=" + secureHash;
        System.out.println("PaymentUrl: " + paymentUrl);

        return CreatePaymentResponse.builder()
                .txnRef(txnRef)
                .paymentUrl(paymentUrl)
                .build();
    }

    @Transactional
    public VnpIpnResponse handleIpn(Map<String, String> params) {

        if (!VnpayUtil.verifySecureHash(params, props.getHashSecret())) {
            return new VnpIpnResponse("97", "Invalid signature");
        }

        String txnRef = params.get("vnp_TxnRef");
        if (txnRef == null) return new VnpIpnResponse("01", "Order not found");

        PaymentTransaction tx = paymentRepo.findByTxnRef(txnRef).orElse(null);
        if (tx == null) return new VnpIpnResponse("01", "Order not found");

        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            return new VnpIpnResponse("00", "OK");
        }

        String respCode = params.get("vnp_ResponseCode");
        String txnStatus = params.get("vnp_TransactionStatus");

        boolean success = "00".equals(respCode) && "00".equals(txnStatus);
        if (!success) {
            tx.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(tx);
            return new VnpIpnResponse("00", "OK");
        }

        tx.setStatus(PaymentStatus.SUCCESS);
        paymentRepo.save(tx);

        userProfileRepository.addTurns(tx.getKeycloakId(), props.getBuyTurnsAmount());

        return new VnpIpnResponse("00", "OK");
    }
}