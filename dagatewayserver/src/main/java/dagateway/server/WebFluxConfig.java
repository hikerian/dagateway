package dagateway.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.web.reactive.config.BlockingExecutionConfigurer;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;



/**
 * @author Dong-il Cho
 */
@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {
	
	public WebFluxConfig() {
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins("*")
			.allowedHeaders("*")
			.exposedHeaders("Content-Disposition")
			.allowedMethods("*");
	}

	@Override
	public void configureBlockingExecution(BlockingExecutionConfigurer configurer) {
		AsyncTaskExecutor executor = new VirtualThreadTaskExecutor();
		configurer.setExecutor(executor);
	}


}
