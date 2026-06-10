package tcp.client.ticketShop.commandHandler;

import core.models.Customer;
import tcp.client.ticketShop.TicketShopStringFormatter;
import tcp.client.TcpClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TCPTicketShopCustomerHandler {

    private final TcpClient tcpClient;

    public TCPTicketShopCustomerHandler(TcpClient tcpClient){
        this.tcpClient = tcpClient;
    }

    public List<Customer> getAllCustomers() {
        try {
            String response = tcpClient.send("customer;getall;");
            return parseCustomers(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Customer createCustomer(
            String username,
            String email,
            LocalDate dateOfBirth
    ) {
        try {
            String msg = String.format(
                    "customer;create;%s,%s,%s",
                    username,
                    email,
                    dateOfBirth
            );
            String response = tcpClient.send(msg);
            return parseCustomer(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Customer getCustomerById(long id) {
        try {
            String msg = String.format("customer;getbyid;%s", id);
            String response = tcpClient.send(msg);
            return parseCustomer(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCustomer(Customer customer) {
        try {
            String msg = String.format(
                    "customer;update;%s,%s,%s,%s",
                    customer.getId(),
                    customer.getUsername(),
                    customer.getEmail(),
                    customer.getDateOfBirth()
            );
            tcpClient.send(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteCustomer(long id) {
        try {
            String msg = String.format("customer;delete;%s", id);
            tcpClient.send(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllCustomers() {
        try {
            tcpClient.send("customer;deleteall;");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Customer parseCustomer(String response) {
        return TicketShopStringFormatter.customerFromString(response);
    }

    private List<Customer> parseCustomers(String response) {
        List<Customer> customers = new ArrayList<>();
        if (response == null || response.isBlank()) return customers;
        String[] parts = response.split(";");
        for (String part : parts) {
            part = part.trim();
            if (!part.isEmpty()) {
                try {
                    customers.add(TicketShopStringFormatter.customerFromString(part));
                } catch (Exception ignored) {}
            }
        }
        return customers;
    }
}
