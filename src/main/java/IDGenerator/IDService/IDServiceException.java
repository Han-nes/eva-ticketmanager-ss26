package IDGenerator.IDService;

public class IDServiceException extends RuntimeException {
    public static String idStoreEmpty = "No more IDs in store";
    public static String lowerLimitHigherThanUpperLimit= "The chosen lower limit is higher than the chosen upper limit.";

    public IDServiceException(String message) {
        super(message);
    }

    public static IDServiceException idStoreEmpty(){
        return new IDServiceException(idStoreEmpty);
    }

    public static IDServiceException lowerLimitHigherThanUpperLimit(){
        return new IDServiceException(lowerLimitHigherThanUpperLimit);
    }
}
