package com.lab1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.lab1.service.CustomerService;
import com.lab1.dto.CustomerDto;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @PostMapping
    public CustomerDto create(@RequestBody CustomerDto dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public CustomerDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<CustomerDto> getAll() {
        return service.getAll();
    }

    @PutMapping("/{id}")
    public CustomerDto update(@PathVariable Long id,
                              @RequestBody CustomerDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
