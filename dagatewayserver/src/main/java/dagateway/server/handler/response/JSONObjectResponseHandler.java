package dagateway.server.handler.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.BodyBuilder;

import dagateway.api.handler.AbstractServiceResponseHandler;
import net.minidev.json.JSONObject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public class JSONObjectResponseHandler extends AbstractServiceResponseHandler<Mono<JSONObject>, JSONObject, JSONObject> {
	private final Logger log = LoggerFactory.getLogger(JSONObjectResponseHandler.class);
	
	
	public JSONObjectResponseHandler() {
	}

	@Override
	protected Mono<JSONObject> resolveBody(Flux<DataBuffer> responseBody) {
		this.log.debug("resolveBody");
		
		ParameterizedTypeReference<JSONObject> jsonType = new ParameterizedTypeReference<>() {};
		ResolvableType resolvableType = ResolvableType.forType(jsonType.getType());
		
		Jackson2JsonDecoder decoder = new Jackson2JsonDecoder();
		Mono<JSONObject> jsonMono = decoder.decodeToMono(responseBody, resolvableType, MediaType.APPLICATION_JSON, null)
				.map(obj -> this.transformer.transform((JSONObject)obj));
		
		return jsonMono;
	}

	@Override
	public Mono<ServerResponse> buildBody(BodyBuilder builder, Mono<JSONObject> body) {
		this.log.debug("buildBody");
		
		return builder.body(BodyInserters.fromProducer(body.map(json -> json.toJSONString()), String.class));
	}

}
