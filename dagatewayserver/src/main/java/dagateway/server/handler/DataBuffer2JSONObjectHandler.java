package dagateway.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;

import dagateway.api.handler.AbstractContentHandler;
import net.minidev.json.JSONObject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class DataBuffer2JSONObjectHandler extends AbstractContentHandler<Flux<DataBuffer>, DataBuffer, JSONObject, JSONObject, Mono<JSONObject>> {
	private final Logger log = LoggerFactory.getLogger(DataBuffer2JSONObjectHandler.class);
	
	
	public DataBuffer2JSONObjectHandler() {
	}

	@Override
	public Mono<JSONObject> handle(Flux<DataBuffer> responseBody) {
//		this.log.debug("resolveBody");
		
		ParameterizedTypeReference<JSONObject> jsonType = new ParameterizedTypeReference<>() {};
		ResolvableType resolvableType = ResolvableType.forType(jsonType.getType());
		
		Jackson2JsonDecoder decoder = new Jackson2JsonDecoder();
		Mono<JSONObject> jsonMono = decoder.decodeToMono(responseBody, resolvableType, MediaType.APPLICATION_JSON, null)
				.map(obj -> this.transformer.transform((JSONObject)obj));
		
		return jsonMono;
	}

	@Override
	protected Mono<JSONObject> wrapSingle(JSONObject value) {
		return Mono.just(value);
	}


}
