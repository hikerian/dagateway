package dagateway.api.resolver.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;


/**
 * @author Dong-il Cho
 */
public class WebSocketMessageResolverFactory {
	private final Logger log = LoggerFactory.getLogger(WebSocketMessageResolverFactory.class);
	
	private Map<MediaType, Supplier<? extends AbstractMessageResolver<?>>> messageResolvers = new ConcurrentHashMap<>();
	
	
	
	public WebSocketMessageResolverFactory() {
		this.messageResolvers.put(MediaType.TEXT_PLAIN, () -> new TextMessageResolver());
		this.messageResolvers.put(MediaType.APPLICATION_OCTET_STREAM, () -> new BinaryMessageResolver());
	}
	
	public void addMessageResolver(MediaType mediaType, Supplier<? extends AbstractMessageResolver<?>> resolverSupplier) {
		Supplier<? extends AbstractMessageResolver<?>> oldResolver = this.messageResolvers.get(mediaType);
		if(oldResolver != null) {
			this.log.warn("Resolver: " + mediaType + " is already exists. It will be replaced." + oldResolver);
		}
		this.messageResolvers.put(mediaType, resolverSupplier);
	}
	
	public <T> WebSocketMessageResolver<T> getMessageResolver(MediaType mediaType) {
		@SuppressWarnings("unchecked")
		Supplier<? extends AbstractMessageResolver<T>> resolverSupplier = (Supplier<? extends AbstractMessageResolver<T>>) this.messageResolvers.get(mediaType);
		if(resolverSupplier == null) {
			return null;
		}
		
		WebSocketMessageResolver<T> messageResolver = resolverSupplier.get();
		
		return messageResolver;
	}


}
