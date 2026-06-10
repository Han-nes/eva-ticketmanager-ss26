package idGenerator.idService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IDServiceParallel implements IDServiceInterface{
    private final long lowerLimit;
    private final long upperLimit;

    private final ConcurrentHashMap<Long, Boolean> idStore;

    public IDServiceParallel(long lowerLimit, long upperLimit){
        if(upperLimit < lowerLimit){
            throw IDServiceException.lowerLimitHigherThanUpperLimit();
        }
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.idStore = new ConcurrentHashMap<>();
    }

    public void generateBatchOfIDs(){
        List<Thread> threads = new ArrayList<>();

        int amountThreads = 10;
        //int amountThreads = Runtime.getRuntime().availableProcessors();

        long range = upperLimit - lowerLimit;
        long partitionSize = range / amountThreads;

        for (int i = 0; i < amountThreads; i++) {
            //Eingabedaten partitioniert
            //Thread idServiceThread = new Thread(new IDService((lowerLimit + i * partitionSize), (lowerLimit + (i+1) * partitionSize), idStore));

            Thread idServiceThread = new Thread(new IDService(lowerLimit, upperLimit, idStore));
            threads.add(idServiceThread);

            System.out.println("Thread: " + i + " gestartet");
            idServiceThread.start();
        }

        try {
            for (int i = 0; i < threads.size(); i++) {
                threads.get(i).join();
                System.out.println("Thread: " + i + " beendet");
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        for(Long id : idStore.keySet()){
            System.out.println("Generierte ID: " + id);
        }
    }

    public void clearIdStore(){
        idStore.clear();
    }

    @Override
    public long getUnusedId() {
        return 2;
    }
}
