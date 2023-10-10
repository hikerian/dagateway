package dagateway.server.resolver.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.composer.DataProxy;
import dagateway.api.composer.MessageSchema;
import dagateway.api.composer.MessageSerializer;
import dagateway.api.composer.builder.json.JsonStreamBuilder;
import dagateway.api.composer.stream.StreamBuffer;
import dagateway.api.context.RouteRequestContext;
import dagateway.api.context.RouteRequestContext.ServiceSpec;
import dagateway.api.resolver.http.MultiBackendResponseResolver;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



/**
 * @author Dongil Cho.
 *
 */
public class JSONGraphMultiResponseResolver extends MultiBackendResponseResolver<Flux<DataBuffer>> {
	private final Logger log = LoggerFactory.getLogger(JSONGraphMultiResponseResolver.class);
	
	
	public JSONGraphMultiResponseResolver() {
	}

	@Override
	public Mono<ServerResponse> resolve(RouteRequestContext routeContext, Flux<ServiceResult<Flux<DataBuffer>>> serviceResults) {
//		this.log.debug("resolve");
		
		Flux<DataBuffer> responseBody = this.resolveBody(routeContext, serviceResults);
		
		ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok();
		
		this.buildHeader(bodyBuilder, routeContext, MediaType.APPLICATION_JSON);

		return bodyBuilder.body((outputMessage, context) -> {
			return outputMessage.writeAndFlushWith(Mono.just(responseBody));
		});
	}
	
	public Flux<DataBuffer> resolveBody(RouteRequestContext routeContext, Flux<ServiceResult<Flux<DataBuffer>>> serviceResults) {
		this.log.debug("flux flux resolve");
				
		MessageSchema messageStructure = routeContext.getMessageStructure();
		MessageSerializer serializer = new MessageSerializer(messageStructure, () -> {
			return new JsonStreamBuilder(StreamBuffer.newDefaultStreamBuffer());
		});
		
		Flux<DataBuffer> responseBody = serviceResults.flatMap(serviceResult -> {
			this.log.debug("flatMap");
			ServiceSpec serviceSpec = serviceResult.getServiceSpec();
			DataProxy dataProxy = serviceSpec.getDataProxy();
			
			Flux<DataBuffer> bodyBuffers = serviceResult.getBody();
			// last marking
			bodyBuffers = bodyBuffers.concatWith(Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(new byte[0])));
			Flux<DataBuffer> resBuffers = bodyBuffers.handle((bodyBuffer, sink) -> {
				if(bodyBuffer.readableByteCount() == 0) { // close
//					this.log.debug("BodyBuffer readableByteCount Zero.");
					dataProxy.finish();
					DataBufferUtils.release(bodyBuffer);
				} else {
//					this.log.debug("BodyBuffer readableByteCount: " + bodyBuffer.readableByteCount());
					dataProxy.push(bodyBuffer, true);
				}

				// Cannot emi more than one data
				byte[] resBuffer = serializer.buildNext();
				if(resBuffer != null && resBuffer.length > 0) {
					sink.next(bodyBuffer.factory().wrap(resBuffer));
				}
			});

			return resBuffers;
		});
		
		return responseBody;
	}
	

}
