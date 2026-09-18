package com.example.lab9.model;

import jakarta.persistence.*;

@Entity
@Table(name = "deposit_transaction")
public class DepositTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    public DepositTransaction() {}

    public DepositTransaction(Double amount, Account account) {
        this.amount = amount;
        this.account = account;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
}
