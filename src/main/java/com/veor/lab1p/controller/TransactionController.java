package com.veor.lab1p.controller;

import com.veor.lab1p.dto.TransactionDTO;
import com.veor.lab1p.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping(value="/api/transactions", produces = "application/json")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;


    @PostMapping
    public ResponseEntity<?> transferMoney(@RequestBody TransactionDTO transactionDTO) {
        try {
            TransactionDTO savedTransaction = transactionService.transferMoney(transactionDTO);
            return ResponseEntity.ok(savedTransaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{accountNumber}")
    public List<TransactionDTO> getTransactionsByAccount(@PathVariable String accountNumber) {
        return transactionService.getTransactionsForAccount(accountNumber);
    }
}