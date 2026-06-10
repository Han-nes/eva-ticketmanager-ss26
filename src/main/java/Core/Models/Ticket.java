package core.models;

import java.time.LocalDate;
import java.util.Objects;

public class Ticket {

    //private final UUID id;
    private final long id;
    private final LocalDate dateOfPurchase;
    private final long customerId;
    private final long eventId;

    public Ticket(
            long id,
            LocalDate dateOfPurchase,
            long customerId,
            long eventId
    ) {
        this.id = id;
        this.dateOfPurchase = dateOfPurchase;
        this.customerId = customerId;
        this.eventId = eventId;
    }

    public long getId() {
        return id;
    }

    public LocalDate getDateOfPurchase() {
        return dateOfPurchase;
    }

    public long getCustomerId() {
        return customerId;
    }

    public long getEventId() {
        return eventId;
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, dateOfPurchase, customerId, eventId);
    }

    @Override
    public boolean equals(Object objectToCompare){
        if (this == objectToCompare) return true;
        if(objectToCompare == null || getClass() != objectToCompare.getClass()) return false;
        Ticket ticketToCompare = (Ticket) objectToCompare;
        return ticketToCompare.getId() == this.getId() &&
                ticketToCompare.getDateOfPurchase().equals(this.getDateOfPurchase()) &&
                ticketToCompare.getCustomerId() == this.getCustomerId() &&
                ticketToCompare.getEventId() == this.getEventId();
    }


}
