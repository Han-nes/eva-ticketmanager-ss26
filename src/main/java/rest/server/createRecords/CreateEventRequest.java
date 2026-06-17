package rest.server.createRecords;

import java.time.LocalDateTime;

public record CreateEventRequest(
        String name,
        String location,
        LocalDateTime time,
        int ticketsAvailable
) {}
