package dagateway.api.resolver.ws;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;


/**
 * @author Dong-il Cho
 */
public interface WebSocketMessageResolver<T> {
	public T extract(WebSocketMessage message);
	public WebSocketMessage build(WebSocketSession session, T message);
	public String getTypeName();
	
}
