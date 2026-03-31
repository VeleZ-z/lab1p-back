package com.veor.lab1p.service;

import com.veor.lab1p.dto.CustomerDTO;
import com.veor.lab1p.entity.Customer;
import com.veor.lab1p.mapper.CustomerMapper;
import com.veor.lab1p.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toDTO).toList();
    }

    public CustomerDTO getCustomerById(Long id) {
        return customerRepository.findById(id).map(customerMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        if (customerDTO.getBalance() == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }

        customerRepository.findByAccountNumber(customerDTO.getAccountNumber())
                .ifPresent(existingCustomer -> {
                    throw new IllegalArgumentException("Account number already exists");
                });

        Customer customer = customerMapper.toEntity(customerDTO);

        try {
            return customerMapper.toDTO(customerRepository.save(customer));
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Account number already exists", exception);
        }
    }
}
