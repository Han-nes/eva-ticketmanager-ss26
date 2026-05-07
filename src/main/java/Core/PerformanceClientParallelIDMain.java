package Core;

import Core.Clients.PerformanceClient;
import IDGenerator.IDService.IDServiceParallel;

public class PerformanceClientParallelIDMain {
    public static void main(String[] args) {
        IDServiceParallel idServiceParallel = new IDServiceParallel(1000000000L, 99999999999L);

        PerformanceClient performanceClient = new PerformanceClient(idServiceParallel);
        performanceClient.run();
    }
}
