package com.lab1.service;

import java.util.List;
import com.lab1.dto.CustomerDto;

public interface CustomerService {

    CustomerDto create(CustomerDto dto);

    CustomerDto getById(Long id);

    List<CustomerDto> getAll();

    CustomerDto update(Long id, CustomerDto dto);

    void delete(Long id);
}
