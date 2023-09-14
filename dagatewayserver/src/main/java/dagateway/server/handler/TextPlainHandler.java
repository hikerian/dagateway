package dagateway.server.handler;

import dagateway.api.handler.AbstractContentHandler;
import reactor.core.publisher.Mono;


public class TextPlainHandler extends AbstractContentHandler<Mono<String>, String, String, String, Mono<String>> {
	
	
	public TextPlainHandler() {
	}

	@Override
	public Mono<String> handle(Mono<String> requestBody) {
		// transform
		Mono<String> transformed = requestBody.map(strData -> this.transformer.transform(strData));

		return transformed;
	}


}
