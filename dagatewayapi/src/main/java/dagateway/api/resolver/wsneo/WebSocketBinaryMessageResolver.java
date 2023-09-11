package dagateway.api.resolver.wsneo;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import dagateway.api.service.ServiceResult;
import dagateway.api.transform.DataTransformer;
import reactor.core.publisher.Mono;


public class WebSocketBinaryMessageResolver implements WebSocketMessageResolver<DataBuffer, DataBuffer> {
	protected DataTransformer<DataBuffer, DataBuffer> transformer;
	
	
	public WebSocketBinaryMessageResolver() {
	}
	
	public void init(DataTransformer<DataBuffer, DataBuffer> transformer) {
		this.transformer = transformer;
	}

	@Override
	public DataBuffer resolveMessage(WebSocketMessage message) {
		DataBuffer payload = message.getPayload();
		payload = this.transformer.transform(payload);
		
		return payload;
	}

	@Override
	public Mono<WebSocketMessage> resolveResult(WebSocketSession session, ServiceResult<Mono<DataBuffer>> result) {
		Mono<DataBuffer> resultBody = result.getBody();

		return resultBody.map(message -> {
			DataBuffer newMessage = this.transformer.transform(message);

			return session.binaryMessage(factory -> factory.wrap(newMessage.asByteBuffer()));
		});
	}

	@Override
	public WebSocketMessage convertMessage(WebSocketSession session, WebSocketMessage message) {
		DataBuffer payload = this.resolveMessage(message);

		return session.binaryMessage(factory -> factory.wrap(payload.asByteBuffer()));
	}

}
