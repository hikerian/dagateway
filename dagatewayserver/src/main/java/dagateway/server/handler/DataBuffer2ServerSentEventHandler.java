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



public class DataBuffer2ServerSentEventHandler extends AbstractContentHandler<Flux<DataBuffer>, DataBuffer, ServerSentEvent<String>, ServerSentEvent<String>, Flux<ServerSentEvent<String>>> {
	private final Logger log = LoggerFactory.getLogger(DataBuffer2ServerSentEventHandler.class);
	
	
	public DataBuffer2ServerSentEventHandler() {
	}

	@Override
	public Flux<ServerSentEvent<String>> handle(Flux<DataBuffer> responseBody) {
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


}
