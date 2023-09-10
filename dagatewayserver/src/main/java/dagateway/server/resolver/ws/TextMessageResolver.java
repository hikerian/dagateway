package dagateway.server.resolver.ws;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import dagateway.api.resolver.ws.AbstractWebSocketMessageResolver;



public class TextMessageResolver extends AbstractWebSocketMessageResolver<String> {
	private final Logger log = LoggerFactory.getLogger(TextMessageResolver.class);
	
	
	public TextMessageResolver() {
	}

	@Override
	public WebSocketMessage resolve(WebSocketSession session, WebSocketMessage message) {
		this.log.debug("resolve");
		
		String payload = message.getPayloadAsText(StandardCharsets.UTF_8);

		// transform
		payload = this.transformer.transform(payload);
		
		return session.textMessage(payload);
	}

}
