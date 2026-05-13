package com.ewallet.wallet_service.controller;
import com.ewallet.wallet_service.dto.SetPinRequest;
import com.ewallet.wallet_service.dto.WalletTransactionDTO;
import com.ewallet.wallet_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor

public class WalletController {
    @Autowired
    private final WalletService walletService;

    @GetMapping("/{userId}/history")
    public ResponseEntity<List<WalletTransactionDTO>> getHistory(@PathVariable Long userId) {
        List<WalletTransactionDTO> history = walletService.getHistory(userId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/set-pin")
    public ResponseEntity<?> setPin(@RequestBody SetPinRequest request) {
        walletService.setPin(request, "1234");
        return ResponseEntity.ok("PIN set successfully");
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

    @PostMapping("/{email}/creditOnTransaction")
    public ResponseEntity<?> creditOnTransaction(@PathVariable String email, @RequestParam BigDecimal amount){
        return ResponseEntity.ok(walletService.creditOnTransaction(email,amount));
    }



    @PostMapping("/{email}/debitOnTransaction")
    public ResponseEntity<?> debit(@PathVariable String email, @RequestParam BigDecimal amount){
        return ResponseEntity.ok(walletService.debitOnTransaction(email,amount));

    }

}