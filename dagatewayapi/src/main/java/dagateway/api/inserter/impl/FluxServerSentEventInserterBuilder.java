package dagateway.api.inserter.impl;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;

import dagateway.api.inserter.AbstractBodyInserterBuilder;
import reactor.core.publisher.Flux;


public class FluxServerSentEventInserterBuilder extends AbstractBodyInserterBuilder<Flux<ServerSentEvent<String>>, ServerHttpResponse> {
	
	
	public FluxServerSentEventInserterBuilder() {
	}

	@Override
	public BodyInserter<?, ServerHttpResponse> getBodyInserter(Flux<ServerSentEvent<String>> data) {
		return BodyInserters.fromServerSentEvents(data);
	}
	
	@Override
	public String supportType() {
		return "reactor.core.publisher.Flux<org.springframework.http.codec.ServerSentEvent<java.lang.String>>";
	}

}
