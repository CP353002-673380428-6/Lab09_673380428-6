package com.example.lab9.controller;

import com.example.lab9.model.Account;
import com.example.lab9.service.AccountService;
import com.example.lab9.service.DepositService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final DepositService depositService;

    public AccountController(AccountService accountService, DepositService depositService) {
        this.accountService = accountService;
        this.depositService = depositService;
    }

    // 1. สร้าง Account ใหม่
    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account created = accountService.createAccount(account);
        return ResponseEntity.ok(created);
    }

    // 2. ดูข้อมูลบัญชีตาม ID
    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    // 3. ฝากเงิน
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Map<String, String>> deposit(@PathVariable Long id, @RequestBody Map<String, Double> request) {
        Double amount = request.get("amount");
        depositService.deposit(id, amount);
        return ResponseEntity.ok(Map.of("message", "Deposit successful"));
    }
}
