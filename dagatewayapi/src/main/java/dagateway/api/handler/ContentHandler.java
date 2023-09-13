package dagateway.api.handler;

import org.reactivestreams.Publisher;

import dagateway.api.context.RouteContext.ServiceSpec;


public interface ContentHandler<P extends Publisher<Cq>, Cq, T, V, R> {
	public R handle(P requestBody, ServiceSpec serviceSpec);
	public String getArgumentTypeName();
	public String getReturnTypeName();
}
