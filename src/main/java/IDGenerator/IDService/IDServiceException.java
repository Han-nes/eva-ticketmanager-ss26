package idGenerator.idService;

public class IDServiceException extends RuntimeException {
    public final static String lowerLimitHigherThanUpperLimit= "The chosen lower limit is higher than the chosen upper limit.";

    public IDServiceException(String message) {
        super(message);
    }

    public static IDServiceException lowerLimitHigherThanUpperLimit(){
        return new IDServiceException(lowerLimitHigherThanUpperLimit);
    }
}
