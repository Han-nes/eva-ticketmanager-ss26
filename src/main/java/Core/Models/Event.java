package core.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Event {

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
        setTicketsAvailable(ticketsAvailable);
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

    public int getTicketsAvailable() {
        return ticketsAvailable.get();
        //return ticketsAvailable.get();
    }

    public void ticketDeleted(long ticketId) {
        this.ticketsSold.remove(ticketId);
        setTicketsAvailable(getTicketsAvailable() + 1);
    }

    public void ticketSold(long ticketId){
        this.ticketsSold.add(ticketId);
        setTicketsAvailable(getTicketsAvailable() - 1);
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
        //this.ticketsAvailable = new AtomicInteger(ticketsAvailable);
    }

    public boolean hasAvailableTickets() {
        return getTicketsAvailable() > 0;
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
                (eventToCompare.getTicketsAvailable() == this.getTicketsAvailable()) &&
                eventToCompare.getTicketsSold().equals(this.ticketsSold);
    }

}
