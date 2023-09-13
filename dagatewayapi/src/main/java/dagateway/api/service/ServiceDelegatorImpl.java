package dagateway.api.service;

import java.util.Map;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import dagateway.api.context.RouteContext.HeaderSpec;
import dagateway.api.context.RouteContext.ServiceSpec;
import dagateway.api.handler.ContentHandler;
import dagateway.api.handler.ContentHandlerFactory;
import dagateway.api.inserter.BodyInserterBuilderFactory;
import dagateway.api.utils.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class ServiceDelegatorImpl<P extends Publisher<Cq>, Cq, Sr> implements ServiceDelegator<P, Cq, Sr> {
	private final Logger log = LoggerFactory.getLogger(ServiceDelegatorImpl.class);
	
	private RequestBodyUriSpec requestBodyUriSpec;
	private ContentHandlerFactory contentHandlerFactory;
	private BodyInserterBuilderFactory bodyInserterBuilderFactory;
	
	private ServiceSpec serviceSpec;
	private String requestResolverTypeName;
	private String resolverTypeName;

	
	public ServiceDelegatorImpl(RequestBodyUriSpec requestBodyUriSpec
			, ContentHandlerFactory contentHandlerFactory
			, BodyInserterBuilderFactory bodyInserterBuilderFactory
			, ServiceSpec serviceSpec
			, String requestResolverTypeName
			, String resolverTypeName) {
		
		this.requestBodyUriSpec = requestBodyUriSpec;
		this.contentHandlerFactory = contentHandlerFactory;
		this.bodyInserterBuilderFactory = bodyInserterBuilderFactory;
		
		this.serviceSpec = serviceSpec;
		this.requestResolverTypeName = requestResolverTypeName;
		this.resolverTypeName = resolverTypeName;
	}
	
	public <R> Mono<ServiceResult<Sr>> run(HttpHeaders clientHeaders, P clientBody) {
		MediaType aggregateType = this.serviceSpec.getAggregateType();
		MediaType serviceRequestType = this.serviceSpec.getServiceRequestType();
		
		// service request header
		Map<String, String> variables = this.serviceSpec.getVariables();
		HeaderSpec headerSpec = this.serviceSpec.getServiceRequestHeaderSpec();
		RequestBodySpec requestBodySpec = this.requestBodyUriSpec.headers(headers -> {
			Utils.filterHeader(clientHeaders, headers, headerSpec, variables);
			headers.setContentType(this.serviceSpec.getServiceRequestType());
		});

		// service request body
		ContentHandler<P, Cq, ?, ?, R> requestHandler = this.contentHandlerFactory.getContentHandler(this.requestResolverTypeName
				, aggregateType
				, serviceRequestType
				, this.serviceSpec.getServiceRequestTransformSpec());

		String returnTypeName = requestHandler.getReturnTypeName();
		R handledValue = requestHandler.handle(clientBody, this.serviceSpec);
		
		System.out.println("returnTypeName: " + returnTypeName);
		System.out.println("handledValue: " + handledValue);
		System.out.println(this.bodyInserterBuilderFactory.getBodyInserter(returnTypeName, handledValue));
		
		RequestHeadersSpec<?> requestHeaderSpec = requestBodySpec.body(this.bodyInserterBuilderFactory.getBodyInserter(returnTypeName, handledValue));
		
		// service request exchange
		ResponseSpec responseSpec = requestHeaderSpec.retrieve();
		Mono<ServiceResult<Sr>> serviceResult = responseSpec
				.toEntityFlux((backendMessage, context) -> backendMessage.getBody())
				.map(responseEntity -> {
					this.log.debug("BACKEND SERVICE RESPONSE STATUS: " + responseEntity.getStatusCode());
					
					HttpHeaders backendHeaders = responseEntity.getHeaders();
					MediaType backendContentType = backendHeaders.getContentType();
					MediaType clientResponseType = this.serviceSpec.getClientResponseType();
					
					if(clientResponseType == null || MediaType.ALL.equalsTypeAndSubtype(clientResponseType)) {
						clientResponseType = backendContentType;
					}
					
					ContentHandler<Flux<DataBuffer>, DataBuffer, ?, ?, Sr> responseHandler = this.contentHandlerFactory.getContentHandler(backendContentType
							, clientResponseType
							, this.resolverTypeName
							, this.serviceSpec.getServiceResponseTransformSpec(backendContentType));
					Sr body = responseHandler.handle(responseEntity.getBody(), this.serviceSpec);
					
					HttpStatus status = responseEntity.getStatusCode();
					HttpHeaders headers = responseEntity.getHeaders();
					HttpHeaders newHttpHeaders = new HttpHeaders();
					newHttpHeaders.putAll(headers);
					newHttpHeaders.setContentType(clientResponseType);
					
					return new ServiceResult<Sr>(this.serviceSpec, status, newHttpHeaders, body, responseHandler.getReturnTypeName());
				})
				.onErrorResume(WebClientResponseException.class, ex -> {
					this.log.debug("BACKEND SERVICE ERROR RESPONSE STATUS: " + this.serviceSpec.createBackendURI() + "(" + ex.getRawStatusCode() + ")");
					
					return Mono.just(new ServiceResult<Sr>(this.serviceSpec, ex));
				});
		
		return serviceResult;
	}


}
