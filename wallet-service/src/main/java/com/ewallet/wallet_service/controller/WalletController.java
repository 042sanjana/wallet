package com.ewallet.wallet_service.controller;
import com.ewallet.wallet_service.dto.SetPinRequest;
import com.ewallet.wallet_service.service.WalletService;
import com.ewallet.wallet_service.entity.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
@RestController
@RequiredArgsConstructor
public class WalletController {
    @Autowired
    private final WalletService walletService;
    @PostMapping("/set-pin")
    public ResponseEntity<?>setPin(@RequestBody SetPinRequest request, @RequestHeader("Authorization")String token) {
        return ResponseEntity.ok(walletService.setPin(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getWallet(@PathVariable Long userId){
        if (walletService.getWalletByUserId(userId) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("wallet not found for user id: " + userId);
        }
            return ResponseEntity.ok(walletService.getWalletByUserId(userId));

    }

    @PostMapping("/{userId}/credit")
    public ResponseEntity<?> credit(@PathVariable Long userId, @RequestParam BigDecimal amount){
            return ResponseEntity.ok(walletService.credit(userId,amount));
        }



    @PostMapping("/{userId}/debit")
    public ResponseEntity<?> debit(@PathVariable Long userId,@RequestParam BigDecimal amount){
        return ResponseEntity.ok(walletService.debit(userId,amount));

    }
}

