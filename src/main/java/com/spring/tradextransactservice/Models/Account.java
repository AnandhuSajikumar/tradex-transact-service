package com.spring.tradextransactservice.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;


    public void debitWallet(BigDecimal amount){
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Debit amount must be positive");
        }

        if(this.balance.compareTo(amount) < 0){
            throw new IllegalStateException("Insufficient Balance");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void creditWallet(BigDecimal amount){
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Credit amount must be positive");
        }
    }
}
