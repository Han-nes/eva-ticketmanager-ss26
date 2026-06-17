package rest.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"rest"})
public class WebTicketShopService {
    public static void main(String[] args) {
        SpringApplication.run(WebTicketShopService.class, args);
    }
}
