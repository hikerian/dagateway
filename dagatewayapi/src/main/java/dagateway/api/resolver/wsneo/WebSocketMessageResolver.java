package dagateway.api.resolver.wsneo;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Mono;


/**
 * 
 * @param <T>
 * @param <V>
 */
public interface WebSocketMessageResolver<T, V> {
	
	/**
	 * websocket to http
	 * receive
	 * @param message
	 * @return
	 */
	public V resolveMessage(WebSocketMessage message); // T

	/**
	 * http to websocket
	 * send
	 * @param result
	 * @return V
	 */
	public Mono<WebSocketMessage> resolveResult(WebSocketSession session, ServiceResult<Mono<T>> result);
	
	/**
	 * websocket to websocket
	 * @param message<T>
	 * @return V
	 */
	public WebSocketMessage convertMessage(WebSocketSession session, WebSocketMessage message);
	

}
