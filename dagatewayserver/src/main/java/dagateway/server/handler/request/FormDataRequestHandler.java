package dagateway.server.handler.request;

import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import dagateway.api.context.RouteContext;
import dagateway.api.handler.AbstractServiceRequestHandler;
import reactor.core.publisher.Mono;



public class FormDataRequestHandler extends AbstractServiceRequestHandler<Mono<MultiValueMap<String, String>>, MultiValueMap<String, String>, MultiValueMap<String, String>, MultiValueMap<String, String>> {
	
	
	public FormDataRequestHandler() {
	}

	@Override
	public RequestHeadersSpec<?> resolveBody(Mono<MultiValueMap<String, String>> requestBody, RequestBodySpec requestBodySpec, RouteContext.ServiceSpec serviceSpec) {
		// transform
		Mono<MultiValueMap<String, String>> transformed = requestBody.map(multivalueMap -> this.transformer.transform(multivalueMap));
		
		return requestBodySpec.body(BodyInserters.fromProducer(transformed, MultiValueMap.class));
	}

}
