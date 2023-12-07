package dagateway.server.resolver.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.composer.DataProxy;
import dagateway.api.composer.MessageSchema;
import dagateway.api.composer.MessageSerializer;
import dagateway.api.composer.builder.json.JsonStreamBuilder;
import dagateway.api.composer.stream.StreamBuffer;
import dagateway.api.context.RouteRequestContext;
import dagateway.api.context.RouteRequestContext.ServiceSpec;
import dagateway.api.resolver.http.SingleBackendResponseResolver;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



/**
 * @author Dongil Cho.
 *
 */
public class JSONGraphSingleResponseResolver extends SingleBackendResponseResolver<Flux<DataBuffer>> {
	private final Logger log = LoggerFactory.getLogger(JSONGraphSingleResponseResolver.class);
	
	
	public JSONGraphSingleResponseResolver() {
	}

	/*
	 * TODO refactoring resolve and resolveBody
	 */
	@Override
	public Mono<ServerResponse> resolve(RouteRequestContext routeContext, Mono<ServiceResult<Flux<DataBuffer>>> serviceResult) {
		this.log.debug("resolve");
		
		MessageSchema messageStructure = routeContext.getMessageStructure();
		MessageSerializer serializer = new MessageSerializer(messageStructure, () -> {
			return new JsonStreamBuilder(StreamBuffer.newDefaultStreamBuffer());
		});
		
		Mono<ServerResponse> serverResponse = serviceResult.flatMap(result -> {
			ServiceSpec serviceSpec = result.getServiceSpec();
			DataProxy dataProxy = serviceSpec.getDataProxy();
			
			Flux<DataBuffer> bodyBuffers = result.getBody();
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
				
				// cannot emi more than one data
				byte[] resBuffer = serializer.buildNext();
				if(resBuffer != null && resBuffer.length > 0) {
					sink.next(bodyBuffer.factory().wrap(resBuffer));
				}
			});
			
			HttpHeaders backendHeaders = result.getHeaders();
			
			ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok();

			this.buildHeader(bodyBuilder, backendHeaders, routeContext, MediaType.APPLICATION_JSON);
			
			return bodyBuilder.body((outputMessage, context) -> {
				return outputMessage.writeAndFlushWith(Mono.just(resBuffers));
			});
		});
		
		return serverResponse;
	}
	
	public Flux<DataBuffer> resolveBody(RouteRequestContext routeContext, ServiceResult<Flux<DataBuffer>> serviceResult) {
//		this.log.debug("resolve");
		
		MessageSchema messageStructure = routeContext.getMessageStructure();
		MessageSerializer serializer = new MessageSerializer(messageStructure, () -> {
			return new JsonStreamBuilder(StreamBuffer.newDefaultStreamBuffer());
		});
		
		ServiceSpec serviceSpec = serviceResult.getServiceSpec();
		DataProxy dataProxy = serviceSpec.getDataProxy();
		
		Flux<DataBuffer> bodyBuffers = serviceResult.getBody();
		// last marking
		bodyBuffers = bodyBuffers.concatWith(Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(new byte[0])));
		
		Flux<DataBuffer> resBuffers = bodyBuffers.handle((bodyBuffer, sink) -> {
			if(bodyBuffer.readableByteCount() == 0) { // close
//				this.log.debug("BodyBuffer readableByteCount Zero.");
				dataProxy.finish();
				DataBufferUtils.release(bodyBuffer);
			} else {
//				this.log.debug("BodyBuffer readableByteCount: " + bodyBuffer.readableByteCount());
				dataProxy.push(bodyBuffer, true);
			}
			
			// cannot emi more than one data
			byte[] resBuffer = serializer.buildNext();
			if(resBuffer != null && resBuffer.length > 0) {
				sink.next(bodyBuffer.factory().wrap(resBuffer));
			}
		});

		return resBuffers;
	}
	

}
