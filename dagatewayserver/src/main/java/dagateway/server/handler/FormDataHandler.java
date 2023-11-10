package dagateway.server.handler;

import org.springframework.util.MultiValueMap;

import dagateway.api.handler.AbstractContentHandler;
import reactor.core.publisher.Mono;



public class FormDataHandler extends AbstractContentHandler<Mono<MultiValueMap<String, String>>, MultiValueMap<String, String>, MultiValueMap<String, String>, MultiValueMap<String, String>, Mono<MultiValueMap<String, String>>> {
	
	
	public FormDataHandler() {
	}

	@Override
	public Mono<MultiValueMap<String, String>> handle(Mono<MultiValueMap<String, String>> requestBody) {
		
		// transform
		Mono<MultiValueMap<String, String>> transformed = requestBody.map(multivalueMap -> this.transformer.transform(multivalueMap));
		
		return transformed;
	}

	@Override
	protected Mono<MultiValueMap<String, String>> wrapSingle(MultiValueMap<String, String> value) {
		return Mono.just(value);
	}

}
