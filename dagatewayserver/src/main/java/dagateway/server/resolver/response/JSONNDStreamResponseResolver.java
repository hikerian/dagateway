package dagateway.server.resolver.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteContext;
import dagateway.api.resolver.MultiBackendResponseResolver;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public class JSONNDStreamResponseResolver extends MultiBackendResponseResolver<Flux<DataBuffer>> {
	private final Logger log = LoggerFactory.getLogger(JSONNDStreamResponseResolver.class);
	
	
	public JSONNDStreamResponseResolver() {
	}

	@Override
	public Mono<ServerResponse> resolve(RouteContext routeContext, Flux<ServiceResult<Flux<DataBuffer>>> serviceResults) {
		this.log.debug("resolve");
		
		DefaultDataBufferFactory databufferFactory = DefaultDataBufferFactory.sharedInstance;
		DefaultDataBuffer newlineBuffer = databufferFactory.wrap(new byte[] {'\n'});
		
		Flux<DataBuffer> responseBodies = serviceResults.flatMap(serviceResult -> {
			Flux<DataBuffer> bodyBuffers = serviceResult.getBody();
			if(bodyBuffers != null) {
				bodyBuffers = bodyBuffers.concatWith(Flux.just(newlineBuffer));
			}
			return bodyBuffers;
		});
		
		ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok();
		
		this.buildHeader(bodyBuilder, routeContext, MediaType.APPLICATION_NDJSON);
		
		return bodyBuilder.body((outputMessage, context) -> {
			return outputMessage.writeAndFlushWith(Mono.just(responseBodies));
		});
	}
	
	public Flux<DataBuffer> resolveBody(RouteContext routeContext, Flux<ServiceResult<Flux<DataBuffer>>> serviceResults) {
		this.log.debug("resolve");
		
		DefaultDataBufferFactory databufferFactory = DefaultDataBufferFactory.sharedInstance;
		DefaultDataBuffer newlineBuffer = databufferFactory.wrap(new byte[] {'\n'});
		
		Flux<DataBuffer> responseBodies = serviceResults.flatMap(serviceResult -> {
			Flux<DataBuffer> bodyBuffers = serviceResult.getBody();
			bodyBuffers = bodyBuffers.concatWith(Flux.just(newlineBuffer));
			return bodyBuffers;
		});
		
		return responseBodies;
	}
	

}
