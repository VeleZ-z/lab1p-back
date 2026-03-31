package com.veor.lab1p.service;

import com.veor.lab1p.dto.CustomerDTO;
import com.veor.lab1p.entity.Customer;
import com.veor.lab1p.mapper.CustomerMapper;
import com.veor.lab1p.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldGetAllCustomers() {
        Customer customerOne = new Customer(1L, "ACC-001", "Ana", "Lopez", 1500.0);
        Customer customerTwo = new Customer(2L, "ACC-002", "Luis", "Perez", 900.0);
        CustomerDTO dtoOne = new CustomerDTO();
        dtoOne.setId(1L);
        dtoOne.setFirstName("Ana");
        dtoOne.setLastName("Lopez");
        dtoOne.setAccountNumber("ACC-001");
        dtoOne.setBalance(1500.0);
        CustomerDTO dtoTwo = new CustomerDTO();
        dtoTwo.setId(2L);
        dtoTwo.setFirstName("Luis");
        dtoTwo.setLastName("Perez");
        dtoTwo.setAccountNumber("ACC-002");
        dtoTwo.setBalance(900.0);

        when(customerRepository.findAll()).thenReturn(List.of(customerOne, customerTwo));
        when(customerMapper.toDTO(customerOne)).thenReturn(dtoOne);
        when(customerMapper.toDTO(customerTwo)).thenReturn(dtoTwo);

        List<CustomerDTO> result = customerService.getAllCustomers();

        assertEquals(2, result.size());
        assertIterableEquals(List.of(dtoOne, dtoTwo), result);
        verify(customerRepository).findAll();
        verify(customerMapper).toDTO(customerOne);
        verify(customerMapper).toDTO(customerTwo);
        verifyNoMoreInteractions(customerRepository, customerMapper);
    }

    @Test
    void shouldGetCustomerById() {
        Customer customer = new Customer(1L, "ACC-001", "Ana", "Lopez", 1500.0);
        CustomerDTO dto = new CustomerDTO();
        dto.setId(1L);
        dto.setFirstName("Ana");
        dto.setLastName("Lopez");
        dto.setAccountNumber("ACC-001");
        dto.setBalance(1500.0);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.toDTO(customer)).thenReturn(dto);

        CustomerDTO result = customerService.getCustomerById(1L);

        assertEquals(dto, result);
        verify(customerRepository).findById(1L);
        verify(customerMapper).toDTO(customer);
        verifyNoMoreInteractions(customerRepository, customerMapper);
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.getCustomerById(999L));

        assertEquals("Cliente no encontrado", exception.getMessage());
        verify(customerRepository).findById(999L);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    void shouldCreateCustomer() {
        CustomerDTO request = new CustomerDTO();
        request.setFirstName("Ana");
        request.setLastName("Lopez");
        request.setAccountNumber("ACC-001");
        request.setBalance(1500.0);

        Customer entityToSave = new Customer(null, "ACC-001", "Ana", "Lopez", 1500.0);
        Customer savedEntity = new Customer(1L, "ACC-001", "Ana", "Lopez", 1500.0);
        CustomerDTO response = new CustomerDTO();
        response.setId(1L);
        response.setFirstName("Ana");
        response.setLastName("Lopez");
        response.setAccountNumber("ACC-001");
        response.setBalance(1500.0);

        when(customerRepository.findByAccountNumber("ACC-001")).thenReturn(Optional.empty());
        when(customerMapper.toEntity(request)).thenReturn(entityToSave);
        when(customerRepository.save(entityToSave)).thenReturn(savedEntity);
        when(customerMapper.toDTO(savedEntity)).thenReturn(response);

        CustomerDTO result = customerService.createCustomer(request);

        assertEquals(response, result);
        verify(customerRepository).findByAccountNumber("ACC-001");
        verify(customerMapper).toEntity(request);
        verify(customerRepository).save(entityToSave);
        verify(customerMapper).toDTO(savedEntity);
        verifyNoMoreInteractions(customerRepository, customerMapper);
    }

    @Test
    void shouldThrowWhenBalanceIsNull() {
        CustomerDTO request = new CustomerDTO();
        request.setFirstName("Ana");
        request.setLastName("Lopez");
        request.setAccountNumber("ACC-001");
        request.setBalance(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer(request));

        assertEquals("Balance cannot be null", exception.getMessage());
        verify(customerRepository, never()).findByAccountNumber(any());
        verify(customerRepository, never()).save(any(Customer.class));
        verifyNoMoreInteractions(customerRepository, customerMapper);
    }

    @Test
    void shouldThrowWhenAccountNumberAlreadyExists() {
        CustomerDTO request = new CustomerDTO();
        request.setFirstName("Ana");
        request.setLastName("Lopez");
        request.setAccountNumber("ACC-001");
        request.setBalance(1500.0);

        when(customerRepository.findByAccountNumber("ACC-001"))
                .thenReturn(Optional.of(new Customer(1L, "ACC-001", "Existing", "Customer", 800.0)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer(request));

        assertEquals("Account number already exists", exception.getMessage());
        verify(customerRepository).findByAccountNumber("ACC-001");
        verify(customerRepository, never()).save(any(Customer.class));
        verifyNoMoreInteractions(customerRepository, customerMapper);
    }

    @Test
    void shouldTranslateDuplicateAccountPersistenceError() {
        CustomerDTO request = new CustomerDTO();
        request.setFirstName("Ana");
        request.setLastName("Lopez");
        request.setAccountNumber("ACC-001");
        request.setBalance(1500.0);

        Customer entityToSave = new Customer(null, "ACC-001", "Ana", "Lopez", 1500.0);

        when(customerRepository.findByAccountNumber("ACC-001")).thenReturn(Optional.empty());
        when(customerMapper.toEntity(request)).thenReturn(entityToSave);
        when(customerRepository.save(entityToSave))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> customerService.createCustomer(request));

        assertEquals("Account number already exists", exception.getMessage());
        verify(customerRepository).findByAccountNumber("ACC-001");
        verify(customerMapper).toEntity(request);
        verify(customerRepository).save(entityToSave);
        verifyNoMoreInteractions(customerRepository, customerMapper);
    }
}
