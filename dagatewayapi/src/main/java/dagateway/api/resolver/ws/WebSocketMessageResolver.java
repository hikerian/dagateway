package dagateway.api.resolver.ws;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;


public interface WebSocketMessageResolver<S> {
	public WebSocketMessage resolve(WebSocketSession session, WebSocketMessage message);
}
