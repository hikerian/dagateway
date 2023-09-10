package dagateway.api.resolver.ws;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.MediaType;

import dagateway.api.context.RouteContext.TransformSpec;
import dagateway.api.transform.AbstractDataTransformer;
import dagateway.api.transform.DataTransformerFactory;
import dagateway.api.utils.Utils;


public class WebSocketMessageResolverFactory {
	private final Logger log = LoggerFactory.getLogger(WebSocketMessageResolverFactory.class);
	
	private AutowireCapableBeanFactory autowireCapableBeanFactory;
	private DataTransformerFactory dataTransformerFactory;
	private Map<MediaType, Class<? extends AbstractWebSocketMessageResolver<?>>> wsResolvers =
			new ConcurrentHashMap<>();
	
	
	public WebSocketMessageResolverFactory() {
	}
	
	public void setAutowireCapableBeanFactory(AutowireCapableBeanFactory autowireCapableBeanFactory) {
		this.autowireCapableBeanFactory = autowireCapableBeanFactory;
	}
	
	public void setDataTransformerFactory(DataTransformerFactory dataTransformerFactory) {
		this.dataTransformerFactory = dataTransformerFactory;
	}
	
	public void addWsMessageResolver(MediaType contentType, Class<? extends AbstractWebSocketMessageResolver<?>> requestResolverClass) {		
		Class<? extends AbstractWebSocketMessageResolver<?>> oldResolver = this.wsResolvers.get(contentType);
		if(oldResolver != null) {
			this.log.warn("Resolver: " + contentType + " is already exists. It will be replaced." + oldResolver);
		}
		this.wsResolvers.put(contentType, requestResolverClass);
	}
	
	public WebSocketMessageResolver<?> buildResolver(MediaType contentType, TransformSpec transformSpec) {
		AbstractWebSocketMessageResolver<?> resolver = null;
		if(contentType == null) {
			resolver = new BypassMessageResolver();
		} else {
			Class<? extends AbstractWebSocketMessageResolver<?>> resolverClass = this.wsResolvers.get(contentType);
			
			ParameterizedType parameterizedType = (ParameterizedType)resolverClass.getGenericSuperclass();
			Type[] supportedTypes = parameterizedType.getActualTypeArguments();
			
			AbstractDataTransformer<?, ?> transformer = 
					(AbstractDataTransformer<?, ?>) this.dataTransformerFactory.newDataTransformer(contentType
							, contentType, supportedTypes[0], supportedTypes[0], transformSpec);
			
			resolver = Utils.newInstance(resolverClass);
			resolver.setDataTransformer(transformer);
		}
		
		this.autowireCapableBeanFactory.autowireBean(resolver);
		
		return resolver;
	}

	
}
