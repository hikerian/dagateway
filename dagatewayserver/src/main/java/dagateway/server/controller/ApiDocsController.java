package dagateway.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.BodyBuilder;

import reactor.core.publisher.Mono;


@Component
public class ApiDocsController {
	private final Logger log = LoggerFactory.getLogger(ApiDocsController.class);
	
	
	public ApiDocsController() {
	}
	
	public Mono<ServerResponse> service(ServerRequest serverRequest) {
		this.log.debug("service");
		
		BodyBuilder bodyBuilder = ServerResponse.ok();
		return bodyBuilder.build();
	}

}
