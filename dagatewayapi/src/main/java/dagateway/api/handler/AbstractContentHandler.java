package dagateway.api.handler;

import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;

import dagateway.api.service.ServiceFault;
import dagateway.api.transform.DataTransformer;



/**
 * @author Dong-il Cho
 * @param <P>
 * @param <Cq>
 * @param <T>
 * @param <V>
 * @param <R>
 */
public abstract class AbstractContentHandler<P extends Publisher<Cq>, Cq, T, V, R>
	implements ContentHandler<P, Cq, T, V, R> {
	
	protected DataTransformer<T, V> transformer;
	protected MediaType fromContentType;
	protected MediaType toContentType;
	
	
	protected AbstractContentHandler() {
	}
	
	public void init(DataTransformer<T, V> transformer, MediaType fromContentType, MediaType toContentType) {
		this.transformer = transformer;
		this.fromContentType = fromContentType;
		this.toContentType = toContentType;
	}
	
	
	@Override
	public abstract R handle(P requestBody);
	
	@Override
	public R handleFault(ServiceFault fault) {
		V transformed = this.transformer.transform(fault);
		return this.wrapSingle(transformed);
	}
	protected abstract R wrapSingle(V value);


}
