package com.veor.lab1p.controller;

import com.veor.lab1p.dto.CustomerDTO;
import com.veor.lab1p.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerFacade;

    public CustomerController(CustomerService customerFacade) {
        this.customerFacade = customerFacade;
    }

    // Obtener todos los clientes
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerFacade.getAllCustomers());
    }

    //  Obtener un cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerFacade.getCustomerById(id));
    }

    //  Crear un nuevo cliente
    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customerDTO) {
        if (customerDTO.getBalance() == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }

        return ResponseEntity.ok(customerFacade.createCustomer(customerDTO));
    }

}