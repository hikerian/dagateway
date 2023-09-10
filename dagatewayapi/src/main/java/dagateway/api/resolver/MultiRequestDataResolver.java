package dagateway.api.resolver;

import org.springframework.web.reactive.function.server.ServerRequest;

import dagateway.api.context.RouteContext;
import reactor.core.publisher.Flux;


public abstract class MultiRequestDataResolver<Cq> extends AbstractClientRequestResolver<Cq, Flux<Cq>> {
	
	
	public MultiRequestDataResolver() {
	}
	
	public Flux<Cq> resolve(RouteContext routeContext, ServerRequest serverRequest) {
		Flux<Cq> resultFlux = this.doResolve(routeContext, serverRequest);
		return resultFlux.defaultIfEmpty(this.emptyValue());
	}
	
	public abstract Flux<Cq> doResolve(RouteContext routeContext, ServerRequest serverRequest);
	public abstract Cq emptyValue();
}
