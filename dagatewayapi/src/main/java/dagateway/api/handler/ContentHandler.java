package dagateway.api.handler;

import org.reactivestreams.Publisher;

import dagateway.api.service.ServiceFault;


public interface ContentHandler<P extends Publisher<Cq>, Cq, T, V, R> {
	public R handle(P requestBody);
	public R handleFault(ServiceFault fault);
	public String getArgumentTypeName();
	public String getReturnTypeName();
}
