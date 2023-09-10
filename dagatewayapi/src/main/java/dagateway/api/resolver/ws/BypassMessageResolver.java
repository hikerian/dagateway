package dagateway.api.resolver.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;



public class BypassMessageResolver extends AbstractWebSocketMessageResolver<String> {
	private final Logger log = LoggerFactory.getLogger(BypassMessageResolver.class);
	
	
	public BypassMessageResolver() {
	}

	@Override
	public WebSocketMessage resolve(WebSocketSession session, WebSocketMessage message) {
		this.log.debug("resolve");
		
		String payload = message.getPayloadAsText();
		return session.textMessage(payload);
	}

}
