package dagateway.server.resolver.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteRequestContext;
import dagateway.api.resolver.http.MultiBackendResponseResolver;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 */
public class NDJSONStreamResponseResolver extends MultiBackendResponseResolver<Flux<DataBuffer>> {
	private final Logger log = LoggerFactory.getLogger(NDJSONStreamResponseResolver.class);
	
	
	public NDJSONStreamResponseResolver() {
	}

	@Override
	public Mono<ServerResponse> resolve(RouteRequestContext routeContext, Flux<ServiceResult<Flux<DataBuffer>>> serviceResults) {
		this.log.debug("resolve");
		
		DefaultDataBufferFactory databufferFactory = DefaultDataBufferFactory.sharedInstance;
		DefaultDataBuffer newlineBuffer = databufferFactory.wrap(new byte[] {'\n'});
		
		Flux<DataBuffer> responseBodies = serviceResults.flatMap(serviceResult -> {
			Flux<DataBuffer> bodyBuffers = serviceResult.getBody();
			if(bodyBuffers != null) {
				bodyBuffers = bodyBuffers.concatWith(Mono.just(newlineBuffer));
			}
			return bodyBuffers;
		});
		
		ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok();
		
		this.buildHeader(bodyBuilder, routeContext, MediaType.APPLICATION_NDJSON);
		
		return bodyBuilder.body((outputMessage, context) -> {
			return outputMessage.writeAndFlushWith(Mono.just(responseBodies));
		});
	}
	
	public Flux<DataBuffer> resolveBody(RouteRequestContext routeContext, Flux<ServiceResult<Flux<DataBuffer>>> serviceResults) {
//		this.log.debug("resolveBody");
		
		DefaultDataBufferFactory databufferFactory = DefaultDataBufferFactory.sharedInstance;
		DefaultDataBuffer newlineBuffer = databufferFactory.wrap(new byte[] {'\n'});
		
		Flux<DataBuffer> responseBodies = serviceResults.flatMap(serviceResult -> {
			Flux<DataBuffer> bodyBuffers = serviceResult.getBody();
			bodyBuffers = bodyBuffers.concatWith(Mono.just(newlineBuffer));
			return bodyBuffers;
		});
		
		return responseBodies;
	}
	

}
