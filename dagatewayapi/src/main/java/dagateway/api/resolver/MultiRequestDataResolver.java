package dagateway.api.resolver;

import org.springframework.web.reactive.function.server.ServerRequest;

import reactor.core.publisher.Flux;


public abstract class MultiRequestDataResolver<Cq> extends AbstractClientRequestResolver<Cq, Flux<Cq>> {
	
	
	public MultiRequestDataResolver() {
	}
	
	public Flux<Cq> resolve(ServerRequest serverRequest) {
		Flux<Cq> resultFlux = this.doResolve(serverRequest);
		return resultFlux.defaultIfEmpty(this.emptyValue());
	}
	
	public abstract Flux<Cq> doResolve(ServerRequest serverRequest);
	public abstract Cq emptyValue();
}
