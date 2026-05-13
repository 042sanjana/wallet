package com.ewallet.wallet_service.service;

import com.ewallet.wallet_service.dto.SetPinRequest;
import com.ewallet.wallet_service.dto.WalletTransactionDTO;
import com.ewallet.wallet_service.entity.Wallet;
import com.ewallet.wallet_service.entity.WalletTransaction;
import com.ewallet.wallet_service.repository.WalletRepository;
import com.ewallet.wallet_service.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    private final WalletTransactionRepository transactionRepository;

    // =========================
    // SET PIN
    // =========================

    public void setPin(SetPinRequest request, String number) {

        Wallet wallet = walletRepository.findByEmail(request.getEmail());

        if (wallet == null) {
            throw new RuntimeException(
                    "Wallet not found for email: " + request.getEmail()
            );
        }

        String hashedPin = passwordEncoder.encode(request.getPin());

        wallet.setPin(hashedPin);

        walletRepository.save(wallet);

        log.info("PIN set successfully for email: {}", request.getEmail());
    }

    // =========================
    // GET WALLET BY USER ID
    // =========================

    public Wallet getWalletByUserId(Long userId) {

        return walletRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Wallet not found for userId: " + userId
                        )
                );
    }

    // =========================
    // CREDIT MONEY
    // =========================

    @Transactional
    public Wallet credit(Long userId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        Wallet wallet = getWalletByUserId(userId);

        wallet.setBalance(
                wallet.getBalance().add(amount)
        );

        Wallet savedWallet = walletRepository.save(wallet);

        logTransaction(
                wallet.getId(),
                WalletTransaction.TransactionType.CREDIT,
                amount,
                wallet.getBalance()
        );

        log.info(
                "Amount {} credited to user {}",
                amount,
                userId
        );

        return savedWallet;
    }

    public Wallet creditOnTransaction(String email, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        Wallet wallet = walletRepository.findByEmail(email);

        wallet.setBalance(
                wallet.getBalance().add(amount)
        );

        Wallet savedWallet = walletRepository.save(wallet);

        logTransaction(
                wallet.getId(),
                WalletTransaction.TransactionType.CREDIT,
                amount,
                wallet.getBalance()
        );

        log.info(
                "Amount {} credited to user {}",
                amount,
                email
        );

        return savedWallet;
    }

    // =========================
    // DEBIT MONEY
    // =========================

    @Transactional
    public Wallet debit(Long userId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        Wallet wallet = getWalletByUserId(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException(
                    "Insufficient balance"
            );
        }

        wallet.setBalance(
                wallet.getBalance().subtract(amount)
        );

        Wallet savedWallet = walletRepository.save(wallet);

        logTransaction(
                wallet.getId(),
                WalletTransaction.TransactionType.DEBIT,
                amount,
                wallet.getBalance()
        );

        log.info(
                "Amount {} debited from user {}",
                amount,
                userId
        );

        return savedWallet;
    }

    public Wallet debitOnTransaction(String email, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        Wallet wallet = walletRepository.findByEmail(email);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException(
                    "Insufficient balance"
            );
        }

        wallet.setBalance(
                wallet.getBalance().subtract(amount)
        );

        Wallet savedWallet = walletRepository.save(wallet);

        logTransaction(
                wallet.getId(),
                WalletTransaction.TransactionType.DEBIT,
                amount,
                wallet.getBalance()
        );

        log.info(
                "Amount {} debited from user {}",
                amount,
                wallet.getId()
        );

        return savedWallet;
    }

    // =========================
    // TRANSACTION HISTORY
    // =========================

    public List<WalletTransactionDTO> getHistory(Long userId) {

        Wallet wallet = getWalletByUserId(userId);

        return transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // SAVE TRANSACTION LOG
    // =========================

    private void logTransaction(
            Long walletId,
            WalletTransaction.TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter
    ) {

        WalletTransaction tx = new WalletTransaction();

        tx.setWalletId(walletId);

        tx.setType(type);

        tx.setAmount(amount);



        transactionRepository.save(tx);
    }

    // =========================
    // DTO CONVERSION
    // =========================

    private WalletTransactionDTO toDTO(WalletTransaction tx) {

        WalletTransactionDTO dto =
                new WalletTransactionDTO();

        dto.setId(tx.getId());

        dto.setWalletId(tx.getWalletId());

        dto.setType(tx.getType());

        dto.setAmount(tx.getAmount());

        dto.setCreatedAt(tx.getCreatedAt());

        return dto;
    }


}