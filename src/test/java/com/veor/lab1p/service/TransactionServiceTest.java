package com.veor.lab1p.service;

import com.veor.lab1p.dto.TransactionDTO;
import com.veor.lab1p.entity.Customer;
import com.veor.lab1p.entity.Transaction;
import com.veor.lab1p.repository.CustomerRepository;
import com.veor.lab1p.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldTransferMoneySuccessfully() {
        Customer sender = new Customer(1L, "ACC-001", "Ana", "Lopez", 1000.0);
        Customer receiver = new Customer(2L, "ACC-002", "Luis", "Perez", 400.0);
        LocalDateTime timestamp = LocalDateTime.of(2026, 3, 31, 16, 0);

        TransactionDTO request = new TransactionDTO();
        request.setSenderAccountNumber("ACC-001");
        request.setReceiverAccountNumber("ACC-002");
        request.setAmount(250.0);
        request.setTimestamp(timestamp);

        when(customerRepository.findByAccountNumber("ACC-001")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("ACC-002")).thenReturn(Optional.of(receiver));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(99L);
            return transaction;
        });

        TransactionDTO result = transactionService.transferMoney(request);

        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals("ACC-001", result.getSenderAccountNumber());
        assertEquals("ACC-002", result.getReceiverAccountNumber());
        assertEquals(250.0, result.getAmount());
        assertEquals(timestamp, result.getTimestamp());
        assertEquals(750.0, sender.getBalance());
        assertEquals(650.0, receiver.getBalance());

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(customerRepository).findByAccountNumber("ACC-001");
        verify(customerRepository).findByAccountNumber("ACC-002");
        verify(customerRepository, times(2)).save(any(Customer.class));
        verify(transactionRepository).save(transactionCaptor.capture());
        verifyNoMoreInteractions(customerRepository, transactionRepository);

        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals("ACC-001", savedTransaction.getSenderAccountNumber());
        assertEquals("ACC-002", savedTransaction.getReceiverAccountNumber());
        assertEquals(250.0, savedTransaction.getAmount());
        assertEquals(timestamp, savedTransaction.getTimestamp());
    }

    @Test
    void shouldThrowWhenSenderNotFound() {
        TransactionDTO request = new TransactionDTO();
        request.setSenderAccountNumber("ACC-404");
        request.setReceiverAccountNumber("ACC-002");
        request.setAmount(100.0);

        when(customerRepository.findByAccountNumber("ACC-404")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney(request));

        assertEquals("La cuenta del remitente no existe.", exception.getMessage());
        verify(customerRepository).findByAccountNumber("ACC-404");
        verify(customerRepository, never()).findByAccountNumber("ACC-002");
        verify(customerRepository, never()).save(any(Customer.class));
        verifyNoInteractions(transactionRepository);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void shouldThrowWhenReceiverNotFound() {
        Customer sender = new Customer(1L, "ACC-001", "Ana", "Lopez", 1000.0);
        TransactionDTO request = new TransactionDTO();
        request.setSenderAccountNumber("ACC-001");
        request.setReceiverAccountNumber("ACC-404");
        request.setAmount(100.0);

        when(customerRepository.findByAccountNumber("ACC-001")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("ACC-404")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney(request));

        assertEquals("La cuenta del receptor no existe.", exception.getMessage());
        verify(customerRepository).findByAccountNumber("ACC-001");
        verify(customerRepository).findByAccountNumber("ACC-404");
        verify(customerRepository, never()).save(any(Customer.class));
        verifyNoInteractions(transactionRepository);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void shouldThrowWhenBalanceInsufficient() {
        Customer sender = new Customer(1L, "ACC-001", "Ana", "Lopez", 100.0);
        Customer receiver = new Customer(2L, "ACC-002", "Luis", "Perez", 400.0);
        TransactionDTO request = new TransactionDTO();
        request.setSenderAccountNumber("ACC-001");
        request.setReceiverAccountNumber("ACC-002");
        request.setAmount(150.0);

        when(customerRepository.findByAccountNumber("ACC-001")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("ACC-002")).thenReturn(Optional.of(receiver));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney(request));

        assertEquals("Saldo insuficiente en la cuenta del remitente.", exception.getMessage());
        assertEquals(100.0, sender.getBalance());
        assertEquals(400.0, receiver.getBalance());
        verify(customerRepository).findByAccountNumber("ACC-001");
        verify(customerRepository).findByAccountNumber("ACC-002");
        verify(customerRepository, never()).save(any(Customer.class));
        verifyNoInteractions(transactionRepository);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        TransactionDTO request = new TransactionDTO();
        request.setSenderAccountNumber("ACC-001");
        request.setReceiverAccountNumber("ACC-002");
        request.setAmount(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney(request));

        assertEquals("El monto es obligatorio.", exception.getMessage());
        verifyNoInteractions(customerRepository, transactionRepository);
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        TransactionDTO request = new TransactionDTO();
        request.setSenderAccountNumber("ACC-001");
        request.setReceiverAccountNumber("ACC-002");
        request.setAmount(-10.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney(request));

        assertEquals("El monto debe ser mayor que cero.", exception.getMessage());
        verifyNoInteractions(customerRepository, transactionRepository);
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        TransactionDTO request = new TransactionDTO();
        request.setSenderAccountNumber("ACC-001");
        request.setReceiverAccountNumber("ACC-002");
        request.setAmount(0.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney(request));

        assertEquals("El monto debe ser mayor que cero.", exception.getMessage());
        verifyNoInteractions(customerRepository, transactionRepository);
    }

    @Test
    void shouldThrowWhenSenderAndReceiverAreTheSameAccount() {
        TransactionDTO request = new TransactionDTO();
        request.setSenderAccountNumber("ACC-001");
        request.setReceiverAccountNumber("ACC-001");
        request.setAmount(100.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> transactionService.transferMoney(request));

        assertEquals("La cuenta del remitente y la del receptor deben ser diferentes.", exception.getMessage());
        verifyNoInteractions(customerRepository, transactionRepository);
    }
}
