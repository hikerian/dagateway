package dagateway.api.handler;

import org.reactivestreams.Publisher;


public interface ContentHandler<P extends Publisher<Cq>, Cq, T, V, R> {
	public R handle(P requestBody);
	public String getArgumentTypeName();
	public String getReturnTypeName();
}
