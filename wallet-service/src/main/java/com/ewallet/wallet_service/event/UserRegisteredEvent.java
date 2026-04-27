package com.ewallet.wallet_service.event;

import lombok.Data;

@Data
public class UserRegisteredEvent {
    private Long userId;
    private String email;
}
