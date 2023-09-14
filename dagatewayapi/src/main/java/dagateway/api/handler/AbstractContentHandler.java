package dagateway.api.handler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;

import dagateway.api.transform.DataTransformer;



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
	public String getArgumentTypeName() {
		Type[] argTypes = this.getTypeArguments();

		return argTypes[0].getTypeName();
	}
	
	@Override
	public String getReturnTypeName() {
		Type[] argTypes = this.getTypeArguments();

		return argTypes[4].getTypeName();
	}
	
	private Type[] getTypeArguments() {
		ParameterizedType genericParent = (ParameterizedType)this.getClass().getGenericSuperclass();
		Type[] argTypes = genericParent.getActualTypeArguments();
		
		return argTypes;
	}



}
