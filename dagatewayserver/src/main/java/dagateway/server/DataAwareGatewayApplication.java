package dagateway.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;



/**
 * @author Dong-il Cho
 */
@SpringBootApplication
@ImportRuntimeHints({GatewayRuntimeHints.class})
public class DataAwareGatewayApplication {
	
	public DataAwareGatewayApplication() {
	}

	public static void main(String[] args) {
		SpringApplication.run(DataAwareGatewayApplication.class, args);
	}

}
