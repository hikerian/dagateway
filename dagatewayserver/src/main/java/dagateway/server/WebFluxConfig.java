package dagateway.server;

import org.springframework.context.annotation.Configuration;
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
	

}
