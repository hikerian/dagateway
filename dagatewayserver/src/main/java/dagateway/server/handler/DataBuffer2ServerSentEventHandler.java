package dagateway.server.handler;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.ServerSentEventHttpMessageReader;
import org.springframework.http.codec.json.Jackson2JsonDecoder;

import dagateway.api.handler.AbstractContentHandler;
import reactor.core.publisher.Flux;



/**
 * @author Dong-il Cho
 */
public class DataBuffer2ServerSentEventHandler extends AbstractContentHandler<Flux<DataBuffer>, DataBuffer, ServerSentEvent<String>, ServerSentEvent<String>, Flux<ServerSentEvent<String>>> {
	private final Logger log = LoggerFactory.getLogger(DataBuffer2ServerSentEventHandler.class);
	
	public static final String ARGUMENT_TYPE = "reactor.core.publisher.Flux<org.springframework.core.io.buffer.DataBuffer>";
	public static final String RETURN_TYPE = "reactor.core.publisher.Flux<org.springframework.http.codec.ServerSentEvent<java.lang.String>>";
	public static final String TRANS_ARGUMENT_TYPE = "org.springframework.http.codec.ServerSentEvent<java.lang.String>";
	public static final String TRANS_RETURN_TYPE = "org.springframework.http.codec.ServerSentEvent<java.lang.String>";
	
	
	public DataBuffer2ServerSentEventHandler() {
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
	public Flux<ServerSentEvent<String>> handle(Flux<DataBuffer> responseBody) {
		this.log.debug("handle");
		
		ParameterizedTypeReference<ServerSentEvent<String>> sseType = new ParameterizedTypeReference<>() {};
		ResolvableType resolvableType = ResolvableType.forType(sseType.getType());
		
		HttpHeaders dummy = new HttpHeaders();
		
		Jackson2JsonDecoder decoder = new Jackson2JsonDecoder();
		ServerSentEventHttpMessageReader reader = new ServerSentEventHttpMessageReader(decoder);
		Flux<Object> sse = reader.read(resolvableType, new ReactiveHttpInputMessage() {
			@Override
			public HttpHeaders getHeaders() {
				return dummy;
			}
			@Override
			public Flux<DataBuffer> getBody() {
				return responseBody;
			}
		}, Collections.emptyMap());
		
		@SuppressWarnings("unchecked")
		Flux<ServerSentEvent<String>> sseMsg = sse.map(resBody -> {
			return this.transformer.transform((ServerSentEvent<String>) resBody);
		});
		
		return sseMsg;
	}

	@Override
	protected Flux<ServerSentEvent<String>> wrapSingle(ServerSentEvent<String> value) {
		return Flux.just(value);
	}


}
