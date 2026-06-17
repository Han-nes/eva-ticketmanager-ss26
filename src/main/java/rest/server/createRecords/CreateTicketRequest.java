package rest.server.createRecords;

public record CreateTicketRequest(
        long customerId,
        long eventId
) {}
