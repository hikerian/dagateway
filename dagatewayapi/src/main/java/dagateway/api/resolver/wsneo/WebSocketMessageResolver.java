package dagateway.api.resolver.wsneo;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;


public interface WebSocketMessageResolver<S> {
	public WebSocketMessage resolve(WebSocketSession session, Mono<S> serviceResult);
	public String getTypeArgument();
}
