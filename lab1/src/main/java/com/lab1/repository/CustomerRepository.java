package com.lab1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lab1.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
