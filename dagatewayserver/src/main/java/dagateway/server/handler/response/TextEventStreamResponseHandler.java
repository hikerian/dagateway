package dagateway.server.handler.response;

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
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.BodyBuilder;

import dagateway.api.handler.AbstractServiceResponseHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public class TextEventStreamResponseHandler extends AbstractServiceResponseHandler<Flux<ServerSentEvent<String>>, ServerSentEvent<String>, ServerSentEvent<String>> {
	private final Logger log = LoggerFactory.getLogger(TextEventStreamResponseHandler.class);
	
	
	public TextEventStreamResponseHandler() {
	}

	@Override
	protected Flux<ServerSentEvent<String>> resolveBody(Flux<DataBuffer> responseBody) {
		this.log.debug("resolveBody======>>");
		
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
	public Mono<ServerResponse> buildBody(BodyBuilder builder, Flux<ServerSentEvent<String>> body) {
		this.log.debug("buildBody");
		
		return builder.body(BodyInserters.fromServerSentEvents(body));
	}

}
