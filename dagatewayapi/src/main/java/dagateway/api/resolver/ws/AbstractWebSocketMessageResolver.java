package dagateway.api.resolver.ws;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import dagateway.api.transform.AbstractDataTransformer;



public abstract class AbstractWebSocketMessageResolver<S> implements WebSocketMessageResolver<S> {
	protected AbstractDataTransformer<S, S> transformer;
	
	
	public AbstractWebSocketMessageResolver() {
	}
	
	@SuppressWarnings("unchecked")
	public void setDataTransformer(AbstractDataTransformer<?, ?> transformer) {
		this.transformer = (AbstractDataTransformer<S, S>)transformer;
	}

	@Override
	public abstract WebSocketMessage resolve(WebSocketSession session, WebSocketMessage message);

}
