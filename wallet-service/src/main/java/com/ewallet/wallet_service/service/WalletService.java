package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.Responsedto;
import com.ewallet.wallet_service.dto.SetPinRequest;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Scanner;

@Service
@RequiredArgsConstructor


public class WalletService {
    private final WalletRepository walletRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public String setPin (SetPinRequest request) {
        Wallet wallet = walletRepository.findByEmail(request.getEmail());
        if (wallet == null) {
            throw new RuntimeException("Wallet not found for email="+request.getEmail());
        }
        String hashedPin = passwordEncoder.encode(request.getPin());
        wallet.setPin(hashedPin);
        walletRepository.save(wallet);
        return "PIN set successfully";
    }

    private boolean verifyPin(Wallet wallet,String pin){
        if(wallet.getPin() == null){
            throw new RuntimeException("Pin not set");
        }
        if(!passwordEncoder.matches(pin,wallet.getPin())){
            throw new RuntimeException("invalid pin");
        }else{
            return true;
        }
    }

    public Wallet getWalletByUserId(Long userId){
        return walletRepository.findByUserId(userId).orElseThrow(()->new RuntimeException("Wallet not found for userId="+userId));
    }


    @Transactional
    public Responsedto credit(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credited amount must be positive");
        }
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Responsedto responsedto= new Responsedto(wallet.getEmail(),wallet.getBalance());
        return  responsedto;
    }
    @Transactional
      public Responsedto debit(Long userId, BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Debited amount must be positive");
    }
    Wallet wallet = getWalletByUserId(userId);

    Scanner sc=new Scanner(System.in);
    String pin=sc.nextLine();

    if (!verifyPin(wallet,pin)){
        throw new RuntimeException("Wrong Pin");
    }

    if (wallet.getBalance().compareTo(amount) < 0) {
        throw new RuntimeException("Insufficient balance");
    }
    wallet.setBalance(wallet.getBalance().subtract(amount));
    walletRepository.save(wallet);
    Responsedto responsedto= new Responsedto(wallet.getEmail(),wallet.getBalance());
    return  responsedto;
    }
}
