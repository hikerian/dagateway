package dagateway.api.resolver.wsneo;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import dagateway.api.service.ServiceResult;
import dagateway.api.transform.DataTransformer;
import reactor.core.publisher.Mono;


public class WebSocketStringMessageResolver implements WebSocketMessageResolver<String> {
	protected DataTransformer<String, String> transformer;
	
	
	public WebSocketStringMessageResolver() {
	}
	
	public void init(DataTransformer<String, String> transformer) {
		this.transformer = transformer;
	}

	public String resolveMessage(WebSocketMessage message) {
		String payload = message.getPayloadAsText();
		payload = this.transformer.transform(payload);
		
		return payload;
	}

	public Mono<WebSocketMessage> resolveResult(WebSocketSession session, ServiceResult<Mono<String>> result) {
		Mono<String> resultBody = result.getBody();

		return resultBody.map(message -> {
			String newMessage = this.transformer.transform(message);

			return session.textMessage(newMessage);
		});
	}

	@Override
	public WebSocketMessage convertToBackendMessage(WebSocketSession session, WebSocketMessage message) {
		String payload = this.resolveMessage(message);

		return session.textMessage(payload);
	}


	@Override
	public WebSocketMessage convertToClientMessage(WebSocketSession session, WebSocketMessage message) {
		String payload = this.resolveMessage(message);

		return session.textMessage(payload);
	}
}
