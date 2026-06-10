package core.services;

import core.models.exceptions.CustomerException;
import core.interfaces.CustomerServiceInterface;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import core.models.Customer;
import core.models.Ticket;
import idGenerator.idService.IDServiceInterface;

public class CustomerService implements CustomerServiceInterface {

    private final Map<Long, Customer> customersById = new ConcurrentHashMap<>();
    private final TicketService ticketService;
    private final IDServiceInterface idService;

    public CustomerService(TicketService ticketService, IDServiceInterface idService) {
        this.ticketService = ticketService;
        this.idService = idService;
    }

    public Customer createCustomer(
        String username,
        String email,
        LocalDate dateOfBirth
    ) throws IllegalArgumentException {
        long id = idService.getUnusedId();
        Customer customer = new Customer(id, username, email, dateOfBirth);
        saveCustomer(customer);

        return customer;
    }

    public Customer getCustomerById(long id) throws CustomerException {
            if (id <= 0 || !customersById.containsKey(id)) {
                throw CustomerException.customerDoesNotExist();
            }

        return clone(customersById.get(id));
    }


    public void updateCustomer(Customer customer) throws CustomerException {
        validateUpdatedCustomer(customer);
        saveCustomer(customer);
    }

    public void addTicketToCustomer(Ticket ticket){
        Customer customer = getCustomerById(ticket.getCustomerId());
        customer.getTicketsBought().add(ticket.getId());
        customersById.put(customer.getId(), customer);
    }

    public void removeTicketFromCustomer(Ticket ticket){
        Customer customer = getCustomerById(ticket.getCustomerId());
        customer.ticketDeleted(ticket.getId());
        customersById.put(customer.getId(), customer);
    }

    public void deleteCustomer(long id) throws IllegalArgumentException {
        Customer customer = customersById.remove(id);
        if (customer != null) {
            List<Long> ticketIds = new ArrayList<>(customer.getTicketsBought());
            ticketIds.forEach(ticketService::deleteTicket);
        }
    }

    public List<Customer> getAllCustomers() {
        List<Customer> allCustomers = new ArrayList<>();
        for(long customerId : customersById.keySet()){
            try {
                allCustomers.add(getCustomerById(customerId));
            } catch (CustomerException customerException){
                System.out.println("Fehler beim Ziehen eines Kunden");
            }
        }
        return allCustomers;
        //return new ArrayList<>(customersById.values());
    }

    public void deleteAllCustomers() {
        customersById.clear();
        ticketService.deleteAllTickets();
    }

    private void validateCustomer(Customer customer){
        if (LocalDate.now().minusYears(18).isBefore(customer.getDateOfBirth())) {
            throw CustomerException.customerUnder18();
        }

        if (
            !customer.getEmail().contains("@") ||
            customer.getEmail().indexOf("@") !=
            customer.getEmail().lastIndexOf("@") ||
            !(customer.getEmail().lastIndexOf(".") >
            customer.getEmail().indexOf("@"))
        ) {
            throw CustomerException.invalidEmail();
        }
    }

    private void validateUpdatedCustomer(Customer updatedCustomer) throws CustomerException {
        getCustomerById(updatedCustomer.getId());
    }

    private void saveCustomer(Customer customer) throws CustomerException{
        validateCustomer(customer);
        customersById.put(customer.getId(), clone(customer));
    }

    private Customer clone(Customer customer){
        Customer customerClone = new Customer(
                customer.getId(),
                customer.getUsername(),
                customer.getEmail(),
                customer.getDateOfBirth()
        );
        customerClone.getTicketsBought().addAll(customer.getTicketsBought());
        return customerClone;
    }
}
