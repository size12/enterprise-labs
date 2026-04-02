package com.lab1.service;

import com.lab1.dto.CustomerDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    CustomerDto create(CustomerDto dto);

    CustomerDto getById(Long id);

    Page<CustomerDto> getAll(Pageable pageable);

    Page<CustomerDto> getAllFiltered(String firstName, String lastName, String email, Pageable pageable);

    CustomerDto update(Long id, CustomerDto dto);

    void delete(Long id);
}
