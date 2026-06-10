package core.models.exceptions;

public class EventException extends RuntimeException {
    public static final String negativeTicketsAvailable = "Number of tickets available must not be negative.";
    public static final String eventDoesNotExist = "Event does not exist";
    public static final String shouldNotReduceAvailableTicketsWithUpdate = "Must not reduce available ticket contingent of existing event.";
    public static final String cantSetEventTimeIntoPast = "DateTime of Event can't be set into the past";

    public EventException(String message) {
        super(message);
    }

    public static EventException negativeTicketsAvailable() {
        return new EventException(negativeTicketsAvailable);
    }
    public static EventException shouldNotReduceAvailableTicketsWithUpdate() {
        return new EventException(shouldNotReduceAvailableTicketsWithUpdate);
    }

    public static EventException eventDoesNotExist() {
        return new EventException(eventDoesNotExist);
    }

    public static EventException cantSetEventTimeIntoPast() {
        return new EventException(cantSetEventTimeIntoPast);
    }
}
