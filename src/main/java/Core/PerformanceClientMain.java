package Core;

import Core.Clients.PerformanceClient;
import IDGenerator.IDService.IDService;

public class PerformanceClientMain {
    public static void main(String[] args) {
        IDService idService = new IDService(1000000000L, 9999999999L);

        PerformanceClient performanceClient = new PerformanceClient(idService);
        performanceClient.run();
    }
}
