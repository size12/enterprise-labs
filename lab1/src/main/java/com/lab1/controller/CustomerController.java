package com.lab1.controller;

import com.lab1.dto.CustomerCreateDto;
import com.lab1.dto.CustomerDto;
import com.lab1.jms.ReportProducer;
import com.lab1.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;
    private final ReportProducer reportProducer;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerDto create(@RequestBody CustomerCreateDto dto) {
        CustomerDto customerDto = new CustomerDto();
        customerDto.setFirstName(dto.getFirstName());
        customerDto.setLastName(dto.getLastName());
        customerDto.setEmail(dto.getEmail());
        return service.create(customerDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CustomerDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Page<CustomerDto> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));

        if (firstName != null || lastName != null || email != null) {
            return service.getAllFiltered(firstName, lastName, email, pageable);
        }

        return service.getAll(pageable);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerDto update(@PathVariable Long id,
                              @RequestBody CustomerDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /**
     * Асинхронная генерация отчёта по клиентам
     * Запрос помещается в очередь и обрабатывается в фоновом режиме
     */
    @PostMapping("/reports/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestParam(defaultValue = "customer-list") String type,
            @RequestParam(defaultValue = "PDF") String format) {
        
        Long requestId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        
        log.info("Запрос на генерацию отчёта: type={}, format={}, requestId={}", type, format, requestId);
        
        // Отправляем запрос в очередь для асинхронной обработки
        reportProducer.requestReport(requestId, type, format);
        
        Map<String, Object> response = new HashMap<>();
        response.put("requestId", requestId);
        response.put("status", "queued");
        response.put("message", "Запрос на генерацию отчёта принят в обработку");
        response.put("reportType", type);
        response.put("format", format);
        
        return ResponseEntity.accepted().body(response);
    }
}
