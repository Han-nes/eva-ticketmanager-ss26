package concurrence;

import idGenerator.idService.IDService;
import idGenerator.idService.IDServiceParallel;
import concurrence.performanceClients.PerformanceClientFutures;
import concurrence.performanceClients.PerformanceClientThreads;
import concurrence.performanceClients.PerformanceClientThreadsCopilot;

public class PerformanceClientMain {
    public static void main(String[] args) {
        IDService idService = new IDService(1000000000L, 9999999999L);
        IDServiceParallel idServiceParallel = new IDServiceParallel(1000000000L, 99999999999L);

        PerformanceClientThreadsCopilot performanceClientThreadsCopilot = new PerformanceClientThreadsCopilot(idService);
        PerformanceClientThreads performanceClientThreads = new PerformanceClientThreads(idService);
        PerformanceClientFutures performanceClientFutures = new PerformanceClientFutures(idService);


        performanceClientThreads.run();
        //performanceClient.testRaceCondition();
    }
}
