package com.example.customer_management_system.controllers;

import com.example.customer_management_system.dto.BulkUploadResponse;
import com.example.customer_management_system.dto.CustomerDTO;
import com.example.customer_management_system.entities.City;
import com.example.customer_management_system.entities.Country;
import com.example.customer_management_system.services.BulkCustomerService;
import com.example.customer_management_system.services.CustomerService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private CustomerService customerService;
    private BulkCustomerService bulkCustomerService;

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        CustomerDTO createdCustomer = customerService.createCustomer(customerDTO);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        try {
            CustomerDTO customer = customerService.getCustomerById(id);
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) {
        CustomerDTO updatedCustomer = customerService.updateCustomer(id, customerDTO);
        return ResponseEntity.ok(updatedCustomer);
    }

    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> getAllCustomers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "asc") String sortDirection) {
        //Sort object
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        //Pageable object
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CustomerDTO> customers = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<CustomerDTO>> getCustomersList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerDTO> customers = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/cities")
    public ResponseEntity<List<City>> getAllCities() {
        try {
            List<City> cities = customerService.getAllCities();
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getAllCountries() {
        try {
            List<Country> countries = customerService.getAllCountries();
            return ResponseEntity.ok(countries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BulkUploadResponse> bulkUploadCustomers(
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            BulkUploadResponse response = new BulkUploadResponse();
            response.setStatus("FAILED");
            response.setMessage("File is required and cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }

        BulkUploadResponse response = bulkCustomerService.startBulkUpload(file);

        if ("FAILED".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.accepted().body(response);
    }

    @GetMapping(value = "/bulk-upload/status/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BulkUploadResponse> getBulkUploadStatus(@PathVariable String jobId) {
        BulkUploadResponse response = bulkCustomerService.getBulkUploadStatus(jobId);
        return ResponseEntity.ok(response);
    }
}
