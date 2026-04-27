package com.ewallet.wallet_service.dto;

import java.math.BigDecimal;

public class TransferRequest {
    private long receiverId;
    private BigDecimal amount;
    private String pin;
}
