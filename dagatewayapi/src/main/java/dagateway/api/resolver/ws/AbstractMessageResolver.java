package dagateway.api.resolver.ws;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;



public abstract class AbstractMessageResolver<T> implements WebSocketMessageResolver<T> {
	
	
	protected AbstractMessageResolver() {
	}
	
	@Override
	public abstract T extract(WebSocketMessage message);
	
	@Override
	public abstract WebSocketMessage build(WebSocketSession session, T message);
	
	@Override
	public String getTypeName() {
		ParameterizedType paramType = (ParameterizedType)this.getClass().getGenericSuperclass();
		Type[] paramTypes = paramType.getActualTypeArguments();
		
		return "reactor.core.publisher.Mono<" + paramTypes[0].getTypeName() + ">";
	}
}
