package dagateway.api.resolver.wsneo;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import dagateway.api.service.ServiceResult;
import dagateway.api.transform.DataTransformer;
import reactor.core.publisher.Mono;


public abstract class AbstractHttpMessageResolver<T, V> implements HttpMessageResolver<T, V> {
	protected DataTransformer<T, V> transformer;
	
	
	protected AbstractHttpMessageResolver() {
	}
	
	@Override
	public V resolveMessage(WebSocketMessage message) {
		T payload = this.getPayload(message);
		V newPayload = this.transformer.transform(payload);

		return newPayload;
	}

	@Override
	public Mono<WebSocketMessage> resolveResult(WebSocketSession session, ServiceResult<Mono<T>> result) {
		Mono<T> resultBody = result.getBody();
		
		return resultBody.map(message -> {
			V newMessage = this.transformer.transform(message);
			return this.buildMessage(newMessage);
		});
	}
	
	protected abstract T getPayload(WebSocketMessage message);
	protected abstract WebSocketMessage buildMessage(V newMessage);
}
