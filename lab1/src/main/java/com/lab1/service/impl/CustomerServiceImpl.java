package com.lab1.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.lab1.service.CustomerService;
import com.lab1.repository.CustomerRepository;
import com.lab1.entity.Customer;
import com.lab1.dto.CustomerDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;

    private CustomerDto mapToDto(Customer customer) {
        return CustomerDto.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    private Customer mapToEntity(CustomerDto dto) {
        return Customer.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .build();
    }

    @Override
    public CustomerDto create(CustomerDto dto) {
        Customer customer = repository.save(mapToEntity(dto));
        return mapToDto(customer);
    }

    @Override
    public CustomerDto getById(Long id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return mapToDto(customer);
    }

    @Override
    public List<CustomerDto> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDto update(Long id, CustomerDto dto) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());

        return mapToDto(repository.save(customer));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
