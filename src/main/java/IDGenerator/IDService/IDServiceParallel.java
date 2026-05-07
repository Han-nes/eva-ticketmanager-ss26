package IDGenerator.IDService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IDServiceParallel implements IDServiceInterface{
    private final long lowerLimit;
    private final long upperLimit;

    public IDServiceParallel(long lowerLimit, long upperLimit){
        if(upperLimit < lowerLimit){
            throw IDServiceException.lowerLimitHigherThanUpperLimit();
        }
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    @Override
    public long getUnusedId() {
        return (long) (Math.random() * (upperLimit - lowerLimit + 1)) + lowerLimit;
    }
}
