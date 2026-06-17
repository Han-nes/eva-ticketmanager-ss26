package rest.server.createRecords;

import java.time.LocalDate;

public record CreateCustomerRequest(
        String username,
        String email,
        LocalDate dateOfBirth
) {}
