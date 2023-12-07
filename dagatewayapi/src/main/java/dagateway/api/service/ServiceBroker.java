package dagateway.api.service;

import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;


/**
 * @author Dong-il Cho
 */
public interface ServiceBroker<P extends Publisher<Cq>, Cq, Sr> {
	public Mono<ServerResponse> run(ServerRequest serverRequest);
}
