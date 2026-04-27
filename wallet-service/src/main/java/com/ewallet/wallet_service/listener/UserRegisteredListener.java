package com.ewallet.wallet_service.listener;

import com.ewallet.wallet_service.event.UserRegisteredEvent;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;


@Component
@RequiredArgsConstructor
@Slf4j


public class UserRegisteredListener {

    private final WalletRepository walletRepository;

    @RabbitListener(queues = "user-registered-queue")
    public void onUserRegistered(UserRegisteredEvent event){
       log.info("Received UserRegisteredEvent for userId={}",event.getUserId());
       if(walletRepository.existsByUserId(event.getUserId())){
           log.warn("Wallet already exists for userId={}",event.getUserId());
           return;
       }


       Wallet wallet=Wallet.builder()
                .userId(event.getUserId())
                .email(event.getEmail())
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);
        log.info("Created wallet for userId={}",event.getUserId());
    }
}
