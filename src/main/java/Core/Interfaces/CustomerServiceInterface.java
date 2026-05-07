package Core.Interfaces;

import Core.Models.Customer;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CustomerServiceInterface {
    Customer createCustomer(String username, String email, LocalDate dateOfBirth) throws IllegalArgumentException;
    Customer getCustomerById(long id);
    void updateCustomer(Customer customer) throws IllegalArgumentException;
    void deleteCustomer(long id) throws IllegalArgumentException;
    List<Customer> getAllCustomers();
    void deleteAllCustomers();
}
