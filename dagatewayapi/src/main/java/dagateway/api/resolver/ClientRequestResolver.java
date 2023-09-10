package dagateway.api.resolver;

import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.server.ServerRequest;

import dagateway.api.context.RouteContext;



public interface ClientRequestResolver<P extends Publisher<Cq>, Cq> {
	public P resolve(RouteContext routeContext, ServerRequest serverRequest);

}
