package core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Customer {

    //private final UUID id;
    private final long id;
    private String username;
    private String email;
    private LocalDate dateOfBirth;
    private final List<Long> ticketsBought = new ArrayList<>();

    public Customer(
        //UUID id,
        long id,
        String username,
        String email,
        LocalDate dateOfBirth
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    @JsonIgnore
    public List<Long> getTicketsBought() {
        return this.ticketsBought;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void ticketDeleted(long ticketId) {
        this.ticketsBought.remove(ticketId);
    }


    @Override
    public int hashCode(){
        return Objects.hash(id, username, email, dateOfBirth, ticketsBought);
    }

    @Override
    public boolean equals(Object objectToCompare){
        if (this == objectToCompare) return true;
        if(objectToCompare == null || getClass() != objectToCompare.getClass()) return false;
        Customer customerToCompare = (Customer) objectToCompare;
        return customerToCompare.getId() == this.getId() &&
                customerToCompare.getUsername().equals(this.getUsername()) &&
                customerToCompare.getEmail().equals(this.getEmail()) &&
                customerToCompare.getDateOfBirth().equals(this.getDateOfBirth()) &&
                customerToCompare.getTicketsBought().equals(this.getTicketsBought());
    }
}
