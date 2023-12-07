package dagateway.api.resolver.http;

import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteRequestContext;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Mono;


/**
 * @author Dong-il Cho
 */
public interface ClientResponseResolver<P extends Publisher<ServiceResult<Sr>>, Sr> {
	public Mono<ServerResponse> resolve(RouteRequestContext routeContext, P serviceResults);
	public String getTypeArgument();
}
