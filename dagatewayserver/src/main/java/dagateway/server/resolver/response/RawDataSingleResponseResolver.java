package dagateway.server.resolver.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteRequestContext;
import dagateway.api.resolver.http.SingleBackendResponseResolver;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 */
public class RawDataSingleResponseResolver extends SingleBackendResponseResolver<Flux<DataBuffer>> {
	private final Logger log = LoggerFactory.getLogger(RawDataSingleResponseResolver.class);

	
	public RawDataSingleResponseResolver() {
	}
	
	@Override
	public Mono<ServerResponse> resolve(RouteRequestContext routeContext, Mono<ServiceResult<Flux<DataBuffer>>> serviceResult) {
		this.log.debug("resolve");
		
		// Single
		return serviceResult.flatMap(result -> {
			HttpHeaders responseHeaders = result.getHeaders();
			Flux<DataBuffer> dataBuffers = result.getBody();
			
			MediaType backendContentType = responseHeaders.getContentType();
			backendContentType = backendContentType == null ? routeContext.getResponseType() : backendContentType;
			final MediaType responseContentType = backendContentType;
			
			ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok();
			
			this.buildHeader(bodyBuilder, responseHeaders, routeContext, responseContentType);
			
			if(dataBuffers == null) {
				return bodyBuilder.build();
			}
			
			return bodyBuilder.body((outputMessage, context) -> {
				return outputMessage.writeAndFlushWith(Mono.just(dataBuffers));
			});
		});
	}
	
	public Flux<DataBuffer> resolveBody(RouteRequestContext routeContext, ServiceResult<Flux<DataBuffer>> serviceResult) {
//		this.log.debug("resolve");
		
		Flux<DataBuffer> dataBuffers = serviceResult.getBody();
		
		return dataBuffers;
	}

}
