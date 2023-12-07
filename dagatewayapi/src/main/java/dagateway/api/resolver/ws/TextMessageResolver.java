package dagateway.api.resolver.ws;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;



/**
 * @author Dong-il Cho
 */
public class TextMessageResolver extends AbstractMessageResolver<String> {
	
	
	public TextMessageResolver() {
	}

	@Override
	public String extract(WebSocketMessage message) {
		return message.getPayloadAsText();
	}

	@Override
	public WebSocketMessage build(WebSocketSession session, String message) {
		return session.textMessage(message);
	}

}
