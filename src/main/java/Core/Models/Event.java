package Core.Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Event {

    //private final UUID id;
    private final long id;
    private String name;
    private String location;
    private LocalDateTime time;
    private AtomicInteger ticketsAvailable;
    private final List<Long> ticketsSold = new ArrayList<>();

    public Event(
        long id,
        String name,
        String location,
        LocalDateTime time,
        int ticketsAvailable
    ) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.time = time;
        this.ticketsAvailable = new AtomicInteger(ticketsAvailable);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public List<Long> getTicketsSold() {
        return this.ticketsSold;
    }

    public AtomicInteger getTicketsAvailable() {
        return ticketsAvailable;
    }

    public void ticketDeleted(long ticketId) {
        this.ticketsSold.remove(ticketId);
        this.ticketsAvailable.addAndGet(1);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public void setTicketsAvailable(int ticketsAvailable) {
        this.ticketsAvailable = new AtomicInteger(ticketsAvailable);
    }

    public void addTicketToTicketsSold(long ticketId){
        this.ticketsSold.add(ticketId);
    }

    public boolean hasAvailableTickets() {
        return ticketsAvailable.get() > 0;
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, name, location, time, ticketsAvailable, ticketsSold);
    }

    @Override
    public boolean equals(Object objectToCompare){
        if (this == objectToCompare) return true;
        if(objectToCompare == null || getClass() != objectToCompare.getClass()) return false;
        Event eventToCompare = (Event) objectToCompare;
        return eventToCompare.getId() == this.getId() &&
                eventToCompare.getName().equals(this.name) &&
                eventToCompare.getLocation().equals(this.location) &&
                eventToCompare.getTime().equals(this.time) &&
                (eventToCompare.getTicketsAvailable().get() == this.ticketsAvailable.get()) &&
                eventToCompare.getTicketsSold().equals(this.ticketsSold);
    }

}
