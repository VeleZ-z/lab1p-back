package com.veor.lab1p.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veor.lab1p.dto.CustomerDTO;
import com.veor.lab1p.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void shouldGetAllCustomers() throws Exception {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(1L);
        customer.setFirstName("Ana");
        customer.setLastName("Lopez");
        customer.setAccountNumber("ACC-001");
        customer.setBalance(1500.0);

        when(customerService.getAllCustomers()).thenReturn(List.of(customer));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Ana"))
                .andExpect(jsonPath("$[0].lastName").value("Lopez"))
                .andExpect(jsonPath("$[0].accountNumber").value("ACC-001"))
                .andExpect(jsonPath("$[0].balance").value(1500.0));
    }

    @Test
    void shouldGetCustomerById() throws Exception {
        CustomerDTO customer = new CustomerDTO();
        customer.setId(1L);
        customer.setFirstName("Ana");
        customer.setLastName("Lopez");
        customer.setAccountNumber("ACC-001");
        customer.setBalance(1500.0);

        when(customerService.getCustomerById(1L)).thenReturn(customer);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Lopez"))
                .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                .andExpect(jsonPath("$.balance").value(1500.0));
    }

    @Test
    void shouldCreateCustomer() throws Exception {
        CustomerDTO request = new CustomerDTO();
        request.setFirstName("Ana");
        request.setLastName("Lopez");
        request.setAccountNumber("ACC-001");
        request.setBalance(1500.0);

        CustomerDTO response = new CustomerDTO();
        response.setId(1L);
        response.setFirstName("Ana");
        response.setLastName("Lopez");
        response.setAccountNumber("ACC-001");
        response.setBalance(1500.0);

        when(customerService.createCustomer(org.mockito.ArgumentMatchers.any(CustomerDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Lopez"))
                .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                .andExpect(jsonPath("$.balance").value(1500.0));
    }
}
