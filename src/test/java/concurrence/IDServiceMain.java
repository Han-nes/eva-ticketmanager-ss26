package concurrence;

import idGenerator.idService.IDService;
import idGenerator.idService.IDServiceParallel;

public class IDServiceMain {
    public static void main(String[] args){
        IDService idService = new IDService(100000000000000000L, 999999999999999999L);
        IDServiceParallel idServiceParallel = new IDServiceParallel(100000000000000000L, 999999999999999999L);

        //Test Einzelfall
        System.out.println("ID (einzeln generiert): " + idService.getUnusedId());


        //Paralleler Test alt
        long startTime = System.currentTimeMillis();
        System.out.println("Start: " + startTime);

        idServiceParallel.generateBatchOfIDs();

        long endTime = System.currentTimeMillis();
        System.out.println("Ende: " + endTime);
        float duration = (float) (endTime - startTime) / 1000;
        System.out.println("Dauer: " + duration + " Sekunden");
    }
}
