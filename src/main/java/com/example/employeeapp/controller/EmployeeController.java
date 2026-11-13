package com.example.employeeapp.controller;

import com.example.employeeapp.model.Employee;
import com.example.employeeapp.repository.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeRepository repository;

    public EmployeeController(EmployeeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Employee createEmployee(@Valid @RequestBody Employee employee) {
        if (repository.existsByEmail(employee.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        employee.setId(null);
        return repository.save(employee);
    }

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee updated) {
        Employee existing = getEmployee(id);
        if (!existing.getEmail().equals(updated.getEmail())
                && repository.existsByEmail(updated.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setDepartment(updated.getDepartment());
        existing.setSalary(updated.getSalary());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        repository.deleteById(id);
    }
}
