package dagateway.api.resolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;

import dagateway.api.service.ServiceResult;



public abstract class AbstractClientResponseResolver<Sr, P extends Publisher<ServiceResult<Sr>>> implements ClientResponseResolver<P, Sr> {
	protected MediaType contentType;
	

	public AbstractClientResponseResolver() {
	}
	
	public void init(MediaType contentType) {
		this.contentType = contentType;
	}
	
	@Override
	public String getTypeArgument() {
		ParameterizedType genericSuperClass = (ParameterizedType)this.getClass().getGenericSuperclass();
		Type srType = genericSuperClass.getActualTypeArguments()[0];

		return srType.getTypeName();
	}
	

}
