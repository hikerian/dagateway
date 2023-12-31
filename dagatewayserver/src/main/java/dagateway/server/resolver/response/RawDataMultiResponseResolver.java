package dagateway.server.resolver.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteRequestContext;
import dagateway.api.context.RouteRequestContext.ResponseSpec;
import dagateway.api.resolver.http.MultiBackendResponseResolver;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 */
public class RawDataMultiResponseResolver extends MultiBackendResponseResolver<Flux<DataBuffer>> {
	private final Logger log = LoggerFactory.getLogger(RawDataMultiResponseResolver.class);

	
	public RawDataMultiResponseResolver() {
	}
	
	@Override
	public Mono<ServerResponse> resolve(RouteRequestContext routeContext, Flux<ServiceResult<Flux<DataBuffer>>> serviceResults) {
		this.log.debug("resolve");
		
		ResponseSpec responseSpec = routeContext.getResponseSpec();
		MediaType responseType = responseSpec.getContentType();
		
		ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok();
		
		this.buildHeader(bodyBuilder, routeContext, responseType);
		
		Flux<DataBuffer> bodyBuffers = serviceResults.flatMap(result -> {
			return result.getBody();
		});
		
		return bodyBuilder.body((outputMessage, context) -> {
			return outputMessage.writeAndFlushWith(Mono.just(bodyBuffers));
		});
	}
	
	public Flux<DataBuffer> resolveBody(RouteRequestContext routeContext, Flux<ServiceResult<Flux<DataBuffer>>> serviceResults) {
//		this.log.debug("resolve");
		
		Flux<DataBuffer> bodyBuffers = serviceResults.flatMap(result -> {
			return result.getBody();
		});
		
		return bodyBuffers;
	}


}
