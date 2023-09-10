package dagateway.api.resolver;

import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteContext;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Mono;


public interface ClientResponseResolver<P extends Publisher<ServiceResult<Sr>>, Sr> {
	public Mono<ServerResponse> resolve(RouteContext routeContext, P serviceResults);
	public String getTypeArgument();
}
