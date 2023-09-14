package dagateway.api.resolver.ws;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;


public interface MessageResolver<T> {
	public T extract(WebSocketMessage message);
	public WebSocketMessage build(WebSocketSession session, T message);
	public String getTypeName();
	
}
