package idGenerator.idService;

import idGenerator.primeNumberGenerator.PrimeNumberGenerator;
import idGenerator.primeNumberGenerator.PrimeNumberGeneratorInterface;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IDService implements Runnable, IDServiceInterface {

    private final ConcurrentHashMap<Long, Boolean> idStore;

    private final PrimeNumberGeneratorInterface primeNumberGenerator;

    public IDService(long lowerLimit, long upperLimit){
        if(upperLimit < lowerLimit) throw IDServiceException.lowerLimitHigherThanUpperLimit();

        this.primeNumberGenerator = new PrimeNumberGenerator(lowerLimit, upperLimit);

        idStore = new ConcurrentHashMap<>();
    }

    public IDService(long lowerLimit, long upperLimit, ConcurrentHashMap<Long, Boolean> idStore){
        if(upperLimit < lowerLimit) throw IDServiceException.lowerLimitHigherThanUpperLimit();

        this.primeNumberGenerator = new PrimeNumberGenerator(lowerLimit, upperLimit);
        this.idStore = idStore;
    }

    private long generateNewId(){
        long possibleId;

        do{
            possibleId = primeNumberGenerator.getRandomPrimeNumberInRange();
        } while(idStore.containsKey(possibleId));
        idStore.put(possibleId, false);
        return possibleId;
    }

    public synchronized long getUnusedId(){
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

    @Override
    public void run() {
        int idsGenerated = 0;
        int amountIDsToBeGenerated = 10;

        while(idsGenerated < amountIDsToBeGenerated){
            generateNewId();
            idsGenerated++;
        }
    }

    public void clearIdStore(){
        idStore.clear();
    }
}
