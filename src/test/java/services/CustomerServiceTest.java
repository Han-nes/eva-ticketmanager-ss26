package services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import Core.Models.exceptions.CustomerException;
import Core.Models.Customer;
import Core.Services.CustomerService;
import Core.Services.TicketService;
import IDGenerator.IDService.IDService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CustomerServiceTest {

    private CustomerService customerService;
    private final IDService idService = new IDService(10000L, 99999L);

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(new TicketService(idService), idService);
    }

    @Nested
    @DisplayName("Create Customer Tests")
    class CreateCustomerTests {

        @Test
        @DisplayName("Should create customer with valid data")
        void shouldCreateCustomerWithValidData() {
            // Arrange
            String username = "testuser";
            String email = "test@example.com";
            LocalDate dateOfBirth = LocalDate.now().minusYears(25);

            // Act
            Customer customer = customerService.createCustomer(
                username,
                email,
                dateOfBirth
            );

            // Assert
            assertNotNull(customer);
            assertNotNull(customer.getId());
            assertEquals(username, customer.getUsername());
            assertEquals(email, customer.getEmail());
            assertEquals(dateOfBirth, customer.getDateOfBirth());
        }

        @Test
        @DisplayName("Should throw exception for underage customer")
        void shouldThrowExceptionForUnderageCustomer() {
            // Arrange
            String username = "younguser";
            String email = "young@example.com";
            LocalDate dateOfBirth = LocalDate.now().minusYears(16); // Under 18

            // Act & Assert
            CustomerException exception = assertThrows(
                CustomerException.class,
                () ->
                    customerService.createCustomer(username, email, dateOfBirth)
            );
            assertEquals("User has to be 18 years old", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid email format")
        void shouldThrowExceptionForInvalidEmail() {
            // Arrange
            String username = "testuser";
            String invalidEmail = "invalid-email";
            LocalDate dateOfBirth = LocalDate.now().minusYears(25);

            // Act & Assert
            CustomerException exception = assertThrows(
                CustomerException.class,
                () ->
                    customerService.createCustomer(
                        username,
                        invalidEmail,
                        dateOfBirth
                    )
            );
            assertEquals("Invalid email", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for email with multiple @ symbols")
        void shouldThrowExceptionForEmailWithMultipleAtSymbols() {
            // Arrange
            String username = "testuser";
            String invalidEmail = "test@@example.com";
            LocalDate dateOfBirth = LocalDate.now().minusYears(25);

            // Act & Assert
            CustomerException exception = assertThrows(
                CustomerException.class,
                () ->
                    customerService.createCustomer(
                        username,
                        invalidEmail,
                        dateOfBirth
                    )
            );
            assertEquals("Invalid email", exception.getMessage());
        }

        @Test
        @DisplayName("Should accept customer who is exactly 18 years old")
        void shouldAcceptCustomerWhoIsExactly18YearsOld() {
            // Arrange
            String username = "youngadult";
            String email = "adult@example.com";
            LocalDate dateOfBirth = LocalDate.now().minusYears(18);

            // Act
            Customer customer = customerService.createCustomer(
                username,
                email,
                dateOfBirth
            );

            // Assert
            assertNotNull(customer);
            assertEquals(username, customer.getUsername());
        }
    }

    @Nested
    @DisplayName("Update Customer Tests")
    class UpdateCustomerTests {

        @BeforeEach
        void setUp() {
            String username = "testuser";
            String email = "test@example.com";
            LocalDate dateOfBirth = LocalDate.now().minusYears(25);

            testCustomer = customerService.createCustomer(
                    username,
                    email,
                    dateOfBirth
            );
        }

        @Test
        @DisplayName("Should update customer with valid data")
        void shouldUpdateCustomerWithValidData() {
            // Arrange
            String username = "John Doe";
            String email = "john@doe.org";
            LocalDate dateOfBirth = LocalDate.now().minusYears(19);
            // Act
            testCustomer.setUsername(username);
            testCustomer.setEmail(email);
            testCustomer.setDateOfBirth(dateOfBirth);
            customerService.updateCustomer(testCustomer);
            Customer updatedCustomer = customerService.getCustomerById(
                    testCustomer.getId()
            );
            // Assert
            assertNotNull(updatedCustomer);
            assertEquals(username, updatedCustomer.getUsername());
            assertEquals(email, updatedCustomer.getEmail());
            assertEquals(dateOfBirth, updatedCustomer.getDateOfBirth());
        }

        @Test
        @DisplayName("Should throw exception for underage customer")
        void shouldThrowExceptionForUnderageCustomer() {
            // Arrange
            LocalDate dateOfBirth = LocalDate.now().minusYears(16); // Under 18

            // Act & Assert
            CustomerException exception = assertThrows(
                    CustomerException.class,
                    () -> {
                        testCustomer.setDateOfBirth(dateOfBirth);
                        customerService.updateCustomer(testCustomer);
                    }
            );
            assertEquals("User has to be 18 years old", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid email format")
        void shouldThrowExceptionForInvalidEmail() {
            // Arrange
            String invalidEmail = "invalid-email";

            // Act & Assert
            CustomerException exception = assertThrows(
                    CustomerException.class,
                    () -> {
                        testCustomer.setEmail(invalidEmail);
                        customerService.updateCustomer(testCustomer);
                    }
            );
            assertEquals("Invalid email", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for email with multiple @ symbols")
        void shouldThrowExceptionForEmailWithMultipleAtSymbols() {
            // Arrange
            String invalidEmail = "test@@example.com";

            // Act & Assert
            CustomerException exception = assertThrows(
                    CustomerException.class,
                    () -> {
                        testCustomer.setEmail(invalidEmail);
                        customerService.updateCustomer(testCustomer);
                    }
            );
            assertEquals("Invalid email", exception.getMessage());
        }

        @Test
        @DisplayName("Should not create Customer via Update")
        void shouldNotCreateCustomerViaUpdate() {
            // Arrange
            Customer customer = new Customer(idService.getUnusedId(), "Name", "mail@mail.de", LocalDate.now().minusYears(20));

            // Act

            // Act + Assert
            CustomerException exception = assertThrows(
                CustomerException.class,
                () -> customerService.updateCustomer(customer)
            );
            assertEquals(CustomerException.customerDoesNotExist, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("CRUD Operations Tests")
    class CrudOperationsTests {

        @Test
        @DisplayName("Should retrieve customer by ID")
        void shouldRetrieveCustomerById() {
            // Arrange
            Customer createdCustomer = customerService.createCustomer(
                "testuser",
                "test@example.com",
                LocalDate.now().minusYears(25)
            );

            // Act
            Customer retrievedCustomer = customerService.getCustomerById(
                createdCustomer.getId()
            );

            // Assert
            assertNotNull(retrievedCustomer);
            assertEquals(createdCustomer.getId(), retrievedCustomer.getId());
            assertEquals(createdCustomer.getUsername(), retrievedCustomer.getUsername());
        }

        @Test
        @DisplayName("Should return Null for non-existent customer ID")
        void shouldReturnNullForNonExistentCustomerId() {
            // Arrange
            long nonExistentId = -1;

            // Assert
            assertThrows(
                    CustomerException.class,
                    () -> customerService.getCustomerById(nonExistentId)
            );
        }

        @Test
        @DisplayName("Should update existing customer")
        void shouldUpdateExistingCustomer() {
            // Arrange
            Customer customer = customerService.createCustomer(
                "oldname",
                "old@example.com",
                LocalDate.now().minusYears(25)
            );
            customer.setUsername("newname");
            customer.setEmail("new@example.com");

            // Act
            customerService.updateCustomer(customer);
            Customer updatedCustomer = customerService.getCustomerById(
                customer.getId()
            );

            // Assert
            assertEquals("newname", updatedCustomer.getUsername());
            assertEquals("new@example.com", updatedCustomer.getEmail());
        }

        @Test
        @DisplayName("Should delete customer by ID")
        void shouldDeleteCustomerById() {
            // Arrange
            Customer customer = customerService.createCustomer(
                "testuser",
                "test@example.com",
                LocalDate.now().minusYears(25)
            );

            // Act
            customerService.deleteCustomer(customer.getId());

            // Assert
            assertFalse(customerService.getAllCustomers().contains(customer));
        }

        @Test
        @DisplayName("Should get all customers")
        void shouldGetAllCustomers() {
            // Arrange
            customerService.createCustomer(
                "user1",
                "user1@example.com",
                LocalDate.now().minusYears(25)
            );
            customerService.createCustomer(
                "user2",
                "user2@example.com",
                LocalDate.now().minusYears(30)
            );

            // Act
            List<Customer> customers = customerService.getAllCustomers();

            // Assert
            assertEquals(2, customers.size());
        }

        @Test
        @DisplayName("Should delete all customers")
        void shouldDeleteAllCustomers() {
            // Arrange
            customerService.createCustomer(
                "user1",
                "user1@example.com",
                LocalDate.now().minusYears(25)
            );
            customerService.createCustomer(
                "user2",
                "user2@example.com",
                LocalDate.now().minusYears(30)
            );

            // Act
            customerService.deleteAllCustomers();
            List<Customer> customers = customerService.getAllCustomers();

            // Assert
            assertTrue(customers.isEmpty());
        }

    }
}
