package com.example.customer_management_system.services;

import com.example.customer_management_system.dto.AddressDTO;
import com.example.customer_management_system.dto.CustomerDTO;
import com.example.customer_management_system.dto.FamilyMemberDTO;
import com.example.customer_management_system.entities.*;
import com.example.customer_management_system.exceptions.CustomerNotFoundException;
import com.example.customer_management_system.exceptions.DuplicateNicException;
import com.example.customer_management_system.repository.CityRepository;
import com.example.customer_management_system.repository.CountryRepository;
import com.example.customer_management_system.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class CustomerService {
    private CustomerRepository customerRepository;
    private CityRepository cityRepository;
    private CountryRepository countryRepository;

    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        if (customerRepository.existsByNicNumber(customerDTO.getNicNumber())) {
            throw new DuplicateNicException("Customer with NIC " + customerDTO.getNicNumber() + " already exists");
        }

        //Validate family members NICs before creating the customer
        if (customerDTO.getFamilyMembers() != null) {
            validateFamilyMembersNicNumbers(customerDTO.getFamilyMembers(), customerDTO.getNicNumber());
        }

        Customer customer = convertToEntity(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return convertToDTO(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        return convertToDTO(customer);
    }

    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

        //Check if NIC is being changed and if new NIC already exists
        if (!existingCustomer.getNicNumber().equals(customerDTO.getNicNumber()) &&
                customerRepository.existsByNicNumber(customerDTO.getNicNumber())) {
            throw new DuplicateNicException("Customer with NIC " + customerDTO.getNicNumber() + " already exists");
        }

        //Validate family members NICs before updating
        if (customerDTO.getFamilyMembers() != null) {
            validateFamilyMembersNicNumbersForUpdate(customerDTO.getFamilyMembers(), customerDTO.getNicNumber(), id);
        }

        updateCustomerEntity(existingCustomer, customerDTO);
        Customer savedCustomer = customerRepository.save(existingCustomer);
        return convertToDTO(savedCustomer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        Page<Customer> customers = customerRepository.findAll(pageable);
        List<CustomerDTO> customerDTOs = customers.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(customerDTOs, pageable, customers.getTotalElements());
    }

    private void validateFamilyMembersNicNumbers(List<FamilyMemberDTO> familyMembers, String parentNic) {
        for (FamilyMemberDTO familyMember : familyMembers) {
            //Check if family member NIC is same as parent
            if (familyMember.getNicNumber().equals(parentNic)) {
                throw new DuplicateNicException("Family member cannot have the same NIC as the parent customer: " + familyMember.getNicNumber());
            }

            //Check if family member NIC already exists and if it's already someone else's family member
            Optional<Customer> existingCustomer = customerRepository.findByNicNumber(familyMember.getNicNumber());
            if (existingCustomer.isPresent()) {
                Customer existing = existingCustomer.get();
                if (existing.getParentCustomer() != null) {
                    throw new DuplicateNicException("Family member with NIC " + familyMember.getNicNumber() + " is already a family member of another customer");
                }
            }
        }

        //Check for duplicate NICs within family members list
        List<String> nic_numbers = familyMembers.stream()
                .map(FamilyMemberDTO::getNicNumber)
                .collect(Collectors.toList());

        if (nic_numbers.size() != nic_numbers.stream().distinct().count()) {
            throw new DuplicateNicException("Duplicate NICs found within family members");
        }
    }

    private void validateFamilyMembersNicNumbersForUpdate(List<FamilyMemberDTO> familyMembers, String parentNic, Long parentId) {
        for (FamilyMemberDTO familyMember : familyMembers) {
            //Check if family member NIC is same as parent
            if (familyMember.getNicNumber().equals(parentNic)) {
                throw new DuplicateNicException("Family member cannot have the same NIC as the parent customer: " + familyMember.getNicNumber());
            }

            //Check if family member NIC already exists in database
            Optional<Customer> existingCustomer = customerRepository.findByNicNumber(familyMember.getNicNumber());
            if (existingCustomer.isPresent()) {
                Customer existing = existingCustomer.get();

                //Already a family member of this same parent - ALLOW
                if (existing.getParentCustomer() != null && existing.getParentCustomer().getId().equals(parentId)) {
                    continue;
                }

                //Standalone customer (parentCustomer is null) - ALLOW conversion to family member
                if (existing.getParentCustomer() == null) {
                    continue; // This is fine, we can convert standalone customer to family member
                }

                //Already a family member of a DIFFERENT parent - NOT ALLOWED
                if (existing.getParentCustomer() != null && !existing.getParentCustomer().getId().equals(parentId)) {
                    throw new DuplicateNicException("Family member with NIC " + familyMember.getNicNumber() + " is already a family member of another customer");
                }
            }
        }

        //Check for duplicate NICs within family members list
        List<String> nicNumbers = familyMembers.stream()
                .map(FamilyMemberDTO::getNicNumber)
                .collect(Collectors.toList());

        if (nicNumbers.size() != nicNumbers.stream().distinct().count()) {
            throw new DuplicateNicException("Duplicate NICs found within family members");
        }
    }

    private void updateCustomerEntity(Customer customer, CustomerDTO dto) {
        customer.setName(dto.getName());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setNicNumber(dto.getNicNumber());

        //Clear existing mobile numbers and add new ones
        customer.getMobileNumbers().clear();
        if (dto.getMobileNumbers() != null) {
            List<MobileNumber> mobileNumbers = dto.getMobileNumbers().stream()
                    .map(mobile -> new MobileNumber(customer, mobile))
                    .collect(Collectors.toList());
            customer.getMobileNumbers().addAll(mobileNumbers);
        }

        //Clear existing addresses and add new ones
        customer.getAddresses().clear();
        if (dto.getAddresses() != null) {
            List<Address> addresses = dto.getAddresses().stream()
                    .map(addressDTO -> {
                        City city = cityRepository.findById(addressDTO.getCityId())
                                .orElseThrow(() -> new RuntimeException("City not found"));
                        return new Address(customer, addressDTO.getAddressLine1(),
                                addressDTO.getAddressLine2(), city);
                    })
                    .collect(Collectors.toList());
            customer.getAddresses().addAll(addresses);
        }

        //Handle family members update
        customer.getFamilyMembers().clear();
        if (dto.getFamilyMembers() != null) {
            List<Customer> familyMembers = new ArrayList<>();
            for (FamilyMemberDTO fm : dto.getFamilyMembers()) {
                //Check if family member already exists as a customer
                Optional<Customer> existingFamilyMember = customerRepository.findByNicNumber(fm.getNicNumber());

                if (existingFamilyMember.isPresent()) {
                    //Set parent to existing family member
                    customer.setParentCustomer(existingFamilyMember.get());
                    continue;
                }

                //Create new family member and assign parent
                Customer familyMember = new Customer();
                familyMember.setName(fm.getFamilyMemberName());
                familyMember.setNicNumber(fm.getNicNumber());
                familyMember.setDateOfBirth(fm.getDateOfBirth());
                familyMember.setParentCustomer(customer);

                familyMembers.add(familyMember);
            }
            customer.setFamilyMembers(familyMembers);
        }
    }

    private Customer convertToEntity(CustomerDTO dto) {
        Customer customer = new Customer(dto.getName(), dto.getDateOfBirth(), dto.getNicNumber());

        //Handle mobile numbers
        if (dto.getMobileNumbers() != null) {
            List<MobileNumber> mobileNumbers = dto.getMobileNumbers().stream()
                    .map(mobile -> new MobileNumber(customer, mobile))
                    .collect(Collectors.toList());
            customer.setMobileNumbers(new HashSet<>(mobileNumbers));
        }

        //Handle addresses
        if (dto.getAddresses() != null) {
            List<Address> addresses = dto.getAddresses().stream()
                    .map(addressDTO -> {
                        City city = cityRepository.findById(addressDTO.getCityId())
                                .orElseThrow(() -> new RuntimeException("City not found"));
                        return new Address(customer, addressDTO.getAddressLine1(),
                                addressDTO.getAddressLine2(), city);
                    })
                    .collect(Collectors.toList());
            customer.setAddresses(new HashSet<>(addresses));
        }

        //Handle family members
        if (dto.getFamilyMembers() != null) {
            List<Customer> familyMembers = new ArrayList<>();
            for (FamilyMemberDTO fm : dto.getFamilyMembers()) {
                //Check if family member already exists as a customer
                Optional<Customer> existingFamilyMember = customerRepository.findByNicNumber(fm.getNicNumber());

                if (existingFamilyMember.isPresent()) {
                    //Set parent to existing family member
                    customer.setParentCustomer(existingFamilyMember.get());
                    continue;
                }

                //Create new family member and assign parent
                Customer familyMember = new Customer();
                familyMember.setName(fm.getFamilyMemberName());
                familyMember.setNicNumber(fm.getNicNumber());
                familyMember.setDateOfBirth(fm.getDateOfBirth());
                familyMember.setParentCustomer(customer);

                familyMembers.add(familyMember);
            }
            customer.setFamilyMembers(familyMembers);
        }

        return customer;
    }

    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO(customer.getName(), customer.getDateOfBirth(), customer.getNicNumber());
        dto.setId(customer.getId());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());

        //Convert mobile numbers
        if (customer.getMobileNumbers() != null) {
            List<String> mobileNumbers = customer.getMobileNumbers().stream()
                    .map(MobileNumber::getMobileNumber)
                    .collect(Collectors.toList());
            dto.setMobileNumbers(mobileNumbers);
        }

        //Convert addresses
        if (customer.getAddresses() != null) {
            List<AddressDTO> addresses = customer.getAddresses().stream()
                    .map(address -> {
                        AddressDTO addressDTO = new AddressDTO();
                        addressDTO.setId(address.getId());
                        addressDTO.setAddressLine1(address.getAddressLine1());
                        addressDTO.setAddressLine2(address.getAddressLine2());
                        addressDTO.setCityId(address.getCity().getId());
                        addressDTO.setCityName(address.getCity().getName());
                        addressDTO.setCountryName(address.getCity().getCountry().getName());
                        return addressDTO;
                    })
                    .collect(Collectors.toList());
            dto.setAddresses(addresses);
        }

        if (customer.getFamilyMembers() != null) {
            dto.setFamilyMembers(
                    customer.getFamilyMembers().stream()
                            .map(fm -> new FamilyMemberDTO(fm.getName(), fm.getNicNumber(), fm.getDateOfBirth()))
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<City> getAllCities() {
        return cityRepository.findAllWithCountry();
    }

    @Transactional(readOnly = true)
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }
}