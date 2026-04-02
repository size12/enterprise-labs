package com.lab1.service.impl;

import com.lab1.dto.CustomerDto;
import com.lab1.entity.Customer;
import com.lab1.exception.CustomerNotFoundException;
import com.lab1.jms.CustomerEventPublisher;
import com.lab1.jms.NotificationProducer;
import com.lab1.repository.CustomerRepository;
import com.lab1.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final NotificationProducer notificationProducer;
    private final CustomerEventPublisher eventPublisher;

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
    @CacheEvict(value = {"customers", "allCustomers"}, allEntries = true)
    @Transactional
    public CustomerDto create(CustomerDto dto) {
        log.info("Создание нового клиента: {}", dto.getEmail());

        // 1. Сохраняем клиента в БД (синхронно)
        Customer customer = repository.save(mapToEntity(dto));

        // 2. Асинхронно отправляем приветственное письмо
        //    Метод возвращает управление немедленно, отправка в очереди
        notificationProducer.sendWelcomeEmail(
            customer.getId(),
            customer.getEmail(),
            customer.getFirstName()
        );

        // 3. Публикуем событие о создании клиента (для аудит и аналитики)
        eventPublisher.publishCustomerCreated(customer.getId(), customer.getEmail());

        log.info("Клиент создан с ID: {}", customer.getId());
        return mapToDto(customer);
    }

    @Override
    @Cacheable(value = "customers", key = "#id")
    public CustomerDto getById(Long id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return mapToDto(customer);
    }

    @Override
    @Cacheable(value = "allCustomers", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<CustomerDto> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::mapToDto);
    }

    @Override
    public Page<CustomerDto> getAllFiltered(String firstName, String lastName, String email, Pageable pageable) {
        Specification<Customer> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (firstName != null && !firstName.isEmpty()) {
                predicates.add(cb.like(root.get("firstName"), "%" + firstName + "%"));
            }
            if (lastName != null && !lastName.isEmpty()) {
                predicates.add(cb.like(root.get("lastName"), "%" + lastName + "%"));
            }
            if (email != null && !email.isEmpty()) {
                predicates.add(cb.like(root.get("email"), "%" + email + "%"));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return repository.findAll(spec, pageable)
                .map(this::mapToDto);
    }

    @Override
    @CacheEvict(value = {"customers", "allCustomers"}, allEntries = true)
    @Transactional
    public CustomerDto update(Long id, CustomerDto dto) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());

        Customer updatedCustomer = repository.save(customer);

        // Публикуем событие об обновлении клиента
        eventPublisher.publishCustomerUpdated(updatedCustomer.getId(), updatedCustomer.getEmail());

        log.info("Клиент обновлён с ID: {}", updatedCustomer.getId());
        return mapToDto(updatedCustomer);
    }

    @Override
    @CacheEvict(value = {"customers", "allCustomers"}, allEntries = true)
    @Transactional
    public void delete(Long id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        
        String email = customer.getEmail();
        repository.deleteById(id);

        // Публикуем событие об удалении клиента
        eventPublisher.publishCustomerDeleted(id, email);

        log.info("Клиент удалён с ID: {}", id);
    }
}
