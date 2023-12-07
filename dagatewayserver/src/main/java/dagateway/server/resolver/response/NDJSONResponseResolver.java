package dagateway.server.resolver.response;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteRequestContext;
import dagateway.api.resolver.http.MultiBackendResponseResolver;
import dagateway.api.service.ServiceResult;
import net.minidev.json.JSONObject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



/**
 * @deprecated... 실제로는 사용할 수 없을 듯....
 */
public class NDJSONResponseResolver extends MultiBackendResponseResolver<Mono<JSONObject>> {
	private final Logger log = LoggerFactory.getLogger(NDJSONResponseResolver.class);
	
	
	public NDJSONResponseResolver() {
	}

	@Override
	public Mono<ServerResponse> resolve(RouteRequestContext routeContext, Flux<ServiceResult<Mono<JSONObject>>> serviceResults) {
		this.log.debug("resolve");
		
		ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok();
		
		this.buildHeader(bodyBuilder, routeContext, MediaType.APPLICATION_NDJSON);
		
		Flux<JSONObject> jsonBodies = serviceResults.flatMap(result -> {
			return result.getBody();
		});
		
		ParameterizedTypeReference<JSONObject> jsonReference = new ParameterizedTypeReference<>() {};
		ResolvableType resolvableType = ResolvableType.forType(jsonReference.getType());
		
		Jackson2JsonEncoder encoder = new Jackson2JsonEncoder();
		encoder.setStreamingMediaTypes(Arrays.asList(MediaType.APPLICATION_NDJSON));
		
		Flux<DataBuffer> dataBody = encoder.encode(jsonBodies, DefaultDataBufferFactory.sharedInstance, resolvableType, MediaType.APPLICATION_NDJSON, null);
		
		return bodyBuilder.body((outputMessage, context) -> {
			return outputMessage.writeAndFlushWith(Mono.just(dataBody));
		});
		
	}
	
	public Flux<DataBuffer> resolveBody(RouteRequestContext routeContext, Flux<ServiceResult<Mono<JSONObject>>> serviceResults) {
//		this.log.debug("resolve");
		
		ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok();
		
		this.buildHeader(bodyBuilder, routeContext, MediaType.APPLICATION_NDJSON);
		
		Flux<JSONObject> jsonBodies = serviceResults.flatMap(result -> {
			return result.getBody();
		});
		
		ParameterizedTypeReference<JSONObject> jsonReference = new ParameterizedTypeReference<>() {};
		ResolvableType resolvableType = ResolvableType.forType(jsonReference.getType());
		
		Jackson2JsonEncoder encoder = new Jackson2JsonEncoder();
		encoder.setStreamingMediaTypes(Arrays.asList(MediaType.APPLICATION_NDJSON));
		
		Flux<DataBuffer> dataBody = encoder.encode(jsonBodies, DefaultDataBufferFactory.sharedInstance, resolvableType, MediaType.APPLICATION_NDJSON, null);
		
		return dataBody;
		
	}


}
