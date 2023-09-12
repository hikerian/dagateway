package dagateway.api.resolver.wsneo;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import dagateway.api.transform.DataTransformer;


public abstract class AbstractWebSocketMessageResolver<T> implements WebSocketMessageResolver<T> {
	protected DataTransformer<T, T> transformer;
	
	
	protected AbstractWebSocketMessageResolver() {
	}
	
	public void init(DataTransformer<T, T> transformer) {
		this.transformer = transformer;
	}
	
	@Override
	public WebSocketMessage convertToBackendMessage(WebSocketSession session, WebSocketMessage message) {
		T payload = this.resolveMessage(message);
		return this.buildMessage(payload);
	}
	
	@Override
	public WebSocketMessage convertToClientMessage(WebSocketSession session, WebSocketMessage message) {
		T payload = this.resolveMessage(message);
		return this.buildMessage(payload);
	}
	
	protected T resolveMessage(WebSocketMessage message) {
		T payload = this.getPayload(message);
		payload = this.transformer.transform(payload);

		return payload;
	}
	
	protected abstract T getPayload(WebSocketMessage message);
	protected abstract WebSocketMessage buildMessage(T newMessage);

}
