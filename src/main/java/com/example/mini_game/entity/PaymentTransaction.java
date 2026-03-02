package com.example.mini_game.entity;

import com.example.mini_game.dto.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "payment_transaction", indexes = {
        @Index(name = "idx_payment_txnref", columnList = "txnRef", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String txnRef;

    @Column(nullable = false)
    private String keycloakId;

    @Column(nullable = false)
    private Long amountVnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String vnpTransactionNo;
    private String vnpResponseCode;
    private String vnpTxnStatus;
    private String vnpPayDate;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;
}