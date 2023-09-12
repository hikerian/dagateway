package dagateway.server.handler.request;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import dagateway.api.context.RouteContext.ServiceSpec;
import dagateway.api.handler.AbstractServiceRequestHandler;
import reactor.core.publisher.Mono;


public class TextPlainRequestHandler extends AbstractServiceRequestHandler<Mono<String>, String, String, String> {
	
	
	public TextPlainRequestHandler() {
	}

	@Override
	public RequestHeadersSpec<?> resolveBody(Mono<String> requestBody, RequestBodySpec requestBodySpec, ServiceSpec serviceSpec) {
		// transform
		Mono<String> transformed = requestBody.map(strData -> this.transformer.transform(strData));
		
		return requestBodySpec.body(BodyInserters.fromProducer(transformed, String.class));
	}

}
