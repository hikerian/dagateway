package dagateway.server.handler;

import org.springframework.util.MultiValueMap;

import dagateway.api.handler.AbstractContentHandler;
import reactor.core.publisher.Mono;



public class FormDataHandler extends AbstractContentHandler<Mono<MultiValueMap<String, String>>
	, MultiValueMap<String, String>
	, MultiValueMap<String, String>
	, MultiValueMap<String, String>
	, Mono<MultiValueMap<String, String>>> {
	
	public static final String ARGUMENT_TYPE = "reactor.core.publisher.Mono<org.springframework.util.MultiValueMap<java.lang.String, java.lang.String>>";
	public static final String RETURN_TYPE = "reactor.core.publisher.Mono<org.springframework.util.MultiValueMap<java.lang.String, java.lang.String>>";
	public static final String TRANS_ARGUMENT_TYPE = "org.springframework.util.MultiValueMap<java.lang.String, java.lang.String>";
	public static final String TRANS_RETURN_TYPE = "org.springframework.util.MultiValueMap<java.lang.String, java.lang.String>";
	
	
	public FormDataHandler() {
	}

	@Override
	public String getArgumentTypeName() {
		return ARGUMENT_TYPE;
	}

	@Override
	public String getReturnTypeName() {
		return RETURN_TYPE;
	}

	@Override
	public String getTransArgumentTypeName() {
		return TRANS_ARGUMENT_TYPE;
	}

	@Override
	public String getTransReturnTypeName() {
		return TRANS_RETURN_TYPE;
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
