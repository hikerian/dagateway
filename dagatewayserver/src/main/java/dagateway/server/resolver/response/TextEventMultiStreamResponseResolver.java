package dagateway.server.resolver.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteRequestContext;
import dagateway.api.resolver.http.MultiBackendResponseResolver;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public class TextEventMultiStreamResponseResolver extends MultiBackendResponseResolver<Flux<ServerSentEvent<String>>> {
	private final Logger log = LoggerFactory.getLogger(TextEventMultiStreamResponseResolver.class);
	
	
	public TextEventMultiStreamResponseResolver() {
		super();
	}

	@Override
	public Mono<ServerResponse> resolve(RouteRequestContext routeContext, Flux<ServiceResult<Flux<ServerSentEvent<String>>>> serviceResults) {
		this.log.debug("resolve");
		
		ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok();
		
		this.buildHeader(bodyBuilder, routeContext, this.contentType);
		
		Flux<ServerSentEvent<String>> serverSentEvents = serviceResults.flatMap(result -> {
			Flux<ServerSentEvent<String>> backendEvents = result.getBody();
			return backendEvents;
		});
		
		return bodyBuilder.body(BodyInserters.fromServerSentEvents(serverSentEvents));
	}
	
	public Flux<ServerSentEvent<String>> resolveBody(RouteRequestContext routeContext, Flux<ServiceResult<Flux<ServerSentEvent<String>>>> serviceResults) {
//		this.log.debug("resolve");
		
		Flux<ServerSentEvent<String>> serverSentEvents = serviceResults.flatMap(result -> {
			Flux<ServerSentEvent<String>> backendEvents = result.getBody();
			return backendEvents;
		});
		
		return serverSentEvents;
	}


}
