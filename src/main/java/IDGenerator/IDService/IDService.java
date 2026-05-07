package IDGenerator.IDService;

import IDGenerator.PrimeNumberGenerator.PrimeNumberGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IDService implements Runnable, IDServiceInterface {

    private final ConcurrentHashMap<Long, Boolean> idStore;

    private final PrimeNumberGenerator primeNumberGenerator;

    public IDService(long lowerLimit, long upperLimit){
        if(upperLimit < lowerLimit){
            throw IDServiceException.lowerLimitHigherThanUpperLimit();
        }
        this.primeNumberGenerator = new PrimeNumberGenerator(lowerLimit, upperLimit);

        idStore = new ConcurrentHashMap<>();
    }

    public IDService(long lowerLimit, long upperLimit, ConcurrentHashMap<Long, Boolean> idStore){
        if(upperLimit < lowerLimit){
            throw IDServiceException.lowerLimitHigherThanUpperLimit();
        }
        this.primeNumberGenerator = new PrimeNumberGenerator(lowerLimit, upperLimit);
        this.idStore = idStore;
    }

    private long generateNewId(){
        long possibleId = -1;
        while((!idStore.containsKey(possibleId))){
            possibleId = primeNumberGenerator.getRandomPrimeNumberInRange();
            if(idStore.containsKey(possibleId)){
                continue;
            }
            idStore.put(possibleId, false);
        }
        return possibleId;
    }

    public long getUnusedId(){
        for(Map.Entry<Long, Boolean> id : idStore.entrySet()){
            if(!id.getValue()){
                idStore.put(id.getKey(), true);
                return id.getKey();
            }
        }
        long id = generateNewId();
        idStore.put(id, true);
        return id;
    }

    public ConcurrentHashMap<Long, Boolean> getIdStore(){
        return this.idStore;
    }

    @Override
    public void run() {
        int idsGenerated = 0;
        int amountIDsToBeGenerated = 5;

        while(idsGenerated < amountIDsToBeGenerated){
            generateNewId();
            idsGenerated++;
        }
    }
}
