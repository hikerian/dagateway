package dagateway.api.resolver.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import dagateway.api.utils.Utils;


public class WebSocketMessageResolverFactory {
	private final Logger log = LoggerFactory.getLogger(WebSocketMessageResolverFactory.class);
	
	private Map<MediaType, Class<? extends AbstractMessageResolver<?>>> messageResolvers = new ConcurrentHashMap<>();
	
	
	
	public WebSocketMessageResolverFactory() {
		this.messageResolvers.put(MediaType.TEXT_PLAIN, TextMessageResolver.class);
		this.messageResolvers.put(MediaType.APPLICATION_OCTET_STREAM, BinaryMessageResolver.class);
	}
	
	public void addMessageResolver(MediaType mediaType, Class<? extends AbstractMessageResolver<?>> resolverClass) {
		Class<? extends AbstractMessageResolver<?>> oldResolver = this.messageResolvers.get(mediaType);
		if(oldResolver != null) {
//			this.log.warn("Resolver: " + mediaType + " is already exists. It will be replaced." + oldResolver);
		}
		this.messageResolvers.put(mediaType, resolverClass);
	}
	
	public <T> WebSocketMessageResolver<T> getMessageResolver(MediaType mediaType) {
		@SuppressWarnings("unchecked")
		Class<? extends AbstractMessageResolver<T>> resolverClass = (Class<? extends AbstractMessageResolver<T>>) this.messageResolvers.get(mediaType);
		if(resolverClass == null) {
			return null;
		}
		
		WebSocketMessageResolver<T> messageResolver = Utils.newInstance(resolverClass);
		
		return messageResolver;
	}


}
