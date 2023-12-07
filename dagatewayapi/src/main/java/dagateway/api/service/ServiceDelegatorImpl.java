package dagateway.api.service;

import java.util.Map;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import dagateway.api.context.RouteRequestContext.HeaderSpec;
import dagateway.api.context.RouteRequestContext.ServiceSpec;
import dagateway.api.context.RouteRequestContext.TransformSpec;
import dagateway.api.handler.ContentHandler;
import dagateway.api.handler.ContentHandlerFactory;
import dagateway.api.inserter.BodyInserterBuilderFactory;
import dagateway.api.utils.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 */
public class ServiceDelegatorImpl<P extends Publisher<Cq>, Cq, Sr> implements ServiceDelegator<P, Cq, Sr> {
	private final Logger log = LoggerFactory.getLogger(ServiceDelegatorImpl.class);
	
	private static final String SERVICE_RESPONSE_TYPENAME = "reactor.core.publisher.Flux<org.springframework.core.io.buffer.DataBuffer>";
	
	private RequestBodyUriSpec requestBodyUriSpec;
	private ContentHandlerFactory contentHandlerFactory;
	private BodyInserterBuilderFactory bodyInserterBuilderFactory;
	private ServiceExceptionResolver exceptionResolver;
	
	private ServiceSpec serviceSpec;
	private String requestResolverTypeName;
	private String responseResolverTypeName;

	
	public ServiceDelegatorImpl(RequestBodyUriSpec requestBodyUriSpec
			, ContentHandlerFactory contentHandlerFactory
			, BodyInserterBuilderFactory bodyInserterBuilderFactory
			, ServiceExceptionResolver exceptionResolver
			, ServiceSpec serviceSpec
			, String requestResolverTypeName
			, String responseResolverTypeName) {
		
		this.requestBodyUriSpec = requestBodyUriSpec;
		this.contentHandlerFactory = contentHandlerFactory;
		this.bodyInserterBuilderFactory = bodyInserterBuilderFactory;
		this.exceptionResolver = exceptionResolver;
		
		this.serviceSpec = serviceSpec;
		this.requestResolverTypeName = requestResolverTypeName;
		this.responseResolverTypeName = responseResolverTypeName;
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
		ContentHandler<P, Cq, ?, ?, R> requestHandler = this.contentHandlerFactory.getContentHandler(aggregateType
				, serviceRequestType
				, this.requestResolverTypeName
				, this.serviceSpec.getServiceRequestTransformSpec());

		String returnTypeName = requestHandler.getReturnTypeName();
		
		R handledValue = requestHandler.handle(clientBody);
		RequestHeadersSpec<?> requestHeaderSpec = requestBodySpec.body(this.bodyInserterBuilderFactory.getBodyInserter(returnTypeName, handledValue));
		
		// service request exchange
		ResponseSpec responseSpec = requestHeaderSpec.retrieve();
		Mono<ServiceResult<Sr>> serviceResult = responseSpec
				.toEntityFlux((backendMessage, context) -> backendMessage.getBody())
				.flatMap(responseEntity -> {
//						this.log.debug("BACKEND SERVICE RESPONSE STATUS: " + responseEntity.getStatusCode());
					
					HttpHeaders backendHeaders = responseEntity.getHeaders();
					MediaType backendContentType = backendHeaders.getContentType();
					MediaType clientResponseType = this.serviceSpec.getClientResponseType();
					
					if(clientResponseType == null || clientResponseType.isWildcardType() || clientResponseType.isWildcardSubtype()) {
						clientResponseType = backendContentType;
					}
					
					ContentHandler<Flux<DataBuffer>, DataBuffer, ?, ?, Sr> responseHandler = this.contentHandlerFactory.getContentHandler(backendContentType
							, clientResponseType
							, ServiceDelegatorImpl.SERVICE_RESPONSE_TYPENAME
							, this.responseResolverTypeName
							, this.serviceSpec.getServiceResponseTransformSpec(backendContentType));
					Sr body = responseHandler.handle(responseEntity.getBody());
					
					HttpStatusCode status = responseEntity.getStatusCode();
					HttpHeaders headers = responseEntity.getHeaders();
					HttpHeaders newHttpHeaders = new HttpHeaders();
					newHttpHeaders.putAll(headers);
					newHttpHeaders.setContentType(clientResponseType);
					
					return Mono.just(new ServiceResult<Sr>(this.serviceSpec, status, newHttpHeaders, body, responseHandler.getReturnTypeName()));
				})
				.onErrorResume((excep) -> {
					this.log.error("exception", excep);
					
					ServiceFault fault = this.exceptionResolver.resolve(excep);
					
					TransformSpec transformSpec = this.serviceSpec.getServiceResponseTransformSpec();
					MediaType backendContentType = transformSpec.getContentType();
					MediaType clientResponseType = this.serviceSpec.getClientResponseType();
					
					if(backendContentType.isWildcardType() || backendContentType.isWildcardSubtype()) {
						backendContentType = MediaType.APPLICATION_JSON;
					}
					if(clientResponseType == null || clientResponseType.isWildcardType() || clientResponseType.isWildcardSubtype()) {
						clientResponseType = backendContentType;
					}
					
					ContentHandler<Flux<DataBuffer>, DataBuffer, ?, ?, Sr> responseHandler = this.contentHandlerFactory.getContentHandler(backendContentType
							, clientResponseType
							, ServiceDelegatorImpl.SERVICE_RESPONSE_TYPENAME
							, this.responseResolverTypeName
							, this.serviceSpec.getServiceResponseTransformSpec(backendContentType));
					Sr body = responseHandler.handleFault(fault);

					HttpStatusCode status = null;
					HttpHeaders headers = null;
					
					if(excep instanceof WebClientResponseException) {
						WebClientResponseException responseException = (WebClientResponseException)excep;
						status = responseException.getStatusCode();
						headers = responseException.getHeaders();
					} else {
						status = HttpStatus.INTERNAL_SERVER_ERROR;
						headers = new HttpHeaders();
					}
					HttpHeaders newHttpHeaders = new HttpHeaders();
					newHttpHeaders.putAll(headers);
					newHttpHeaders.setContentType(clientResponseType);
					
					return Mono.just(new ServiceResult<Sr>(this.serviceSpec, status, newHttpHeaders, body, responseHandler.getReturnTypeName()));
				});
		
		return serviceResult;
	}

	@Override
	public String toString() {
		return "ServiceDelegatorImpl [serviceSpec=" + this.serviceSpec.getName() + "]";
	}


}
