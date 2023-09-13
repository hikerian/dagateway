package dagateway.api.resolver;

import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;


public abstract class AbstractClientRequestResolver<Cq, P extends Publisher<Cq>> implements ClientRequestResolver<P, Cq> {
	protected MediaType from;
	protected MediaType to;
	
	
	protected AbstractClientRequestResolver() {
	}
	
	public void init(MediaType from, MediaType to) {
		this.from = from;
		this.to = to;
	}
	
	@Override
	public abstract P resolve(ServerRequest serverRequest);
	

}
