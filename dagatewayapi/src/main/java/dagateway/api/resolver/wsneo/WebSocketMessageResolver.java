package dagateway.api.resolver.wsneo;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;


/**
 * 
 * @param <T>
 * @param <V>
 */
public interface WebSocketMessageResolver<T> {
	/**
	 * client websocket to backend websocket
	 * @param message<T>
	 * @return T
	 */
	public WebSocketMessage convertToBackendMessage(WebSocketSession session, WebSocketMessage message);

	/**
	 * backend websocket to client websocket
	 * @param message<T>
	 * @return T
	 */
	public WebSocketMessage convertToClientMessage(WebSocketSession session, WebSocketMessage message);

}
