package com.example.customer_management_system.repository;

import com.example.customer_management_system.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByNicNumber(String nicNumber);

    boolean existsByNicNumber(String nicNumber);

//    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.mobileNumbers LEFT JOIN FETCH c.addresses a LEFT JOIN FETCH a.city cy LEFT JOIN FETCH cy.country WHERE c.id = :id")
//    Optional<Customer> findByIdWithDetails(@Param("id") Long id);

//    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.mobileNumbers LEFT JOIN FETCH c.addresses WHERE c.parentCustomer IS NULL")
//    Page<Customer> findAllMainCustomersWithDetails(Pageable pageable);
//
//    @Query("SELECT c FROM Customer c WHERE c.parentCustomer.id = :parentId")
//    List<Customer> findByParentCustomerId(@Param("parentId") Long parentId);

//    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:name% OR c.nicNumber LIKE %:nic%")
//    Page<Customer> findByNameContainingOrNicNumberContaining(@Param("name") String name, @Param("nic") String nic, Pageable pageable);

    @Query(value = "SELECT c FROM Customer c LEFT JOIN FETCH c.mobileNumbers LEFT JOIN FETCH c.addresses WHERE c.parentCustomer IS NULL",
            countQuery = "SELECT count(c) FROM Customer c WHERE c.parentCustomer IS NULL")
    Page<Customer> findAllMainCustomersWithDetails(Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.parentCustomer.id = :parentId")
    List<Customer> findByParentCustomerId(@Param("parentId") Long parentId);

    // Uncommented and fixed the customer details query
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.mobileNumbers LEFT JOIN FETCH c.addresses a LEFT JOIN FETCH a.city cy LEFT JOIN FETCH cy.country WHERE c.id = :id")
    Optional<Customer> findByIdWithDetails(@Param("id") Long id);

    // Uncommented the search query - this should work fine for pagination since no JOIN FETCH
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:name% OR c.nicNumber LIKE %:nic%")
    Page<Customer> findByNameContainingOrNicNumberContaining(@Param("name") String name, @Param("nic") String nic, Pageable pageable);
}
