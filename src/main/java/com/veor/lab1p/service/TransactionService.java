package com.veor.lab1p.service;

import com.veor.lab1p.dto.TransactionDTO;
import com.veor.lab1p.entity.Customer;
import com.veor.lab1p.entity.Transaction;
import com.veor.lab1p.repository.CustomerRepository;
import com.veor.lab1p.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    public TransactionService(TransactionRepository transactionRepository, CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public TransactionDTO transferMoney(TransactionDTO transactionDTO) {
        if (transactionDTO == null) {
            throw new IllegalArgumentException("La transaccion es obligatoria.");
        }

        if (transactionDTO.getSenderAccountNumber() == null || transactionDTO.getReceiverAccountNumber() == null) {
            throw new IllegalArgumentException("Los numeros de cuenta del remitente y receptor son obligatorios.");
        }

        if (transactionDTO.getAmount() == null) {
            throw new IllegalArgumentException("El monto es obligatorio.");
        }

        if (transactionDTO.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }

        if (transactionDTO.getSenderAccountNumber().equals(transactionDTO.getReceiverAccountNumber())) {
            throw new IllegalArgumentException("La cuenta del remitente y la del receptor deben ser diferentes.");
        }

        Customer sender = customerRepository.findByAccountNumber(transactionDTO.getSenderAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del remitente no existe."));
        Customer receiver = customerRepository.findByAccountNumber(transactionDTO.getReceiverAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del receptor no existe."));

        if (sender.getBalance() < transactionDTO.getAmount()) {
            throw new IllegalArgumentException("Saldo insuficiente en la cuenta del remitente.");
        }

        sender.setBalance(sender.getBalance() - transactionDTO.getAmount());
        receiver.setBalance(receiver.getBalance() + transactionDTO.getAmount());

        customerRepository.save(sender);
        customerRepository.save(receiver);

        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(sender.getAccountNumber());
        transaction.setReceiverAccountNumber(receiver.getAccountNumber());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setTimestamp(transactionDTO.getTimestamp() != null ? transactionDTO.getTimestamp() : LocalDateTime.now());

        transaction = transactionRepository.save(transaction);

        TransactionDTO savedTransaction = new TransactionDTO();
        savedTransaction.setId(transaction.getId());
        savedTransaction.setSenderAccountNumber(transaction.getSenderAccountNumber());
        savedTransaction.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        savedTransaction.setAmount(transaction.getAmount());
        savedTransaction.setTimestamp(transaction.getTimestamp());

        return savedTransaction;
    }

    public List<TransactionDTO> getTransactionsForAccount(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findBySenderAccountNumberOrReceiverAccountNumber(accountNumber, accountNumber);
        return transactions.stream().map(transaction -> {
            TransactionDTO dto = new TransactionDTO();
            dto.setId(transaction.getId());
            dto.setSenderAccountNumber(transaction.getSenderAccountNumber());
            dto.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
            dto.setAmount(transaction.getAmount());
            dto.setTimestamp(transaction.getTimestamp());
            return dto;
        }).collect(Collectors.toList());
    }
}
