package dagateway.server.handler;

import dagateway.api.handler.AbstractContentHandler;
import reactor.core.publisher.Mono;


public class TextPlainHandler extends AbstractContentHandler<Mono<String>, String, String, String, Mono<String>> {
	public static final String ARGUMENT_TYPE = "reactor.core.publisher.Mono<java.lang.String>";
	public static final String RETURN_TYPE = "reactor.core.publisher.Mono<java.lang.String>";
	public static final String TRANS_ARGUMENT_TYPE = "java.lang.String";
	public static final String TRANS_RETURN_TYPE = "java.lang.String";
	
	
	public TextPlainHandler() {
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
	public Mono<String> handle(Mono<String> requestBody) {
		// transform
		Mono<String> transformed = requestBody.map(strData -> this.transformer.transform(strData));

		return transformed;
	}

	@Override
	protected Mono<String> wrapSingle(String value) {
		return Mono.just(value);
	}


}
