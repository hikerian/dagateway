package dagateway.server.resolver.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteRequestContext;
import dagateway.api.resolver.http.SingleBackendResponseResolver;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public class TextEventSingleStreamResponseResolver extends SingleBackendResponseResolver<Flux<ServerSentEvent<String>>> {
	private final Logger log = LoggerFactory.getLogger(TextEventSingleStreamResponseResolver.class);
	
	
	public TextEventSingleStreamResponseResolver() {
		super();
	}

	@Override
	public Mono<ServerResponse> resolve(RouteRequestContext routeContext, Mono<ServiceResult<Flux<ServerSentEvent<String>>>> serviceResults) {
		this.log.debug("resolve");
		
		HttpHeaders backendHeaders = new HttpHeaders();
		Mono<ServerResponse> serverResponse = serviceResults.flatMap(result -> {
			Flux<ServerSentEvent<String>> serverSentEvent = result.getBody();
			
			ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok();
			
			this.buildHeader(bodyBuilder, backendHeaders, routeContext, this.contentType);
			
			return bodyBuilder.body(BodyInserters.fromServerSentEvents(serverSentEvent));
		});

		return serverResponse;
	}
	
	public Flux<ServerSentEvent<String>> resolveBody(RouteRequestContext routeContext, ServiceResult<Flux<ServerSentEvent<String>>> serviceResults) {
//		this.log.debug("resolve");
		
		Flux<ServerSentEvent<String>> serverSentEvent = serviceResults.getBody();
		
		return serverSentEvent;
	}


}
