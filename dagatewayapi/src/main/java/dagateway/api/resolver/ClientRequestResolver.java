package dagateway.api.resolver;

import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.server.ServerRequest;



public interface ClientRequestResolver<P extends Publisher<Cq>, Cq> {
	public P resolve(ServerRequest serverRequest);

}
