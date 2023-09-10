package dagateway.api.service;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import dagateway.api.context.RouteContext.ServiceSpec;
import dagateway.api.handler.ServiceHandlerFactory;
import dagateway.api.handler.ServiceRequestHandler;
import dagateway.api.handler.ServiceResponseHandler;
import reactor.core.publisher.Mono;



public class ServiceDelegatorImpl<P extends Publisher<Cq>, Cq, Sr> implements ServiceDelegator<P, Cq, Sr> {
	private final Logger log = LoggerFactory.getLogger(ServiceDelegatorImpl.class);
	
	private RequestBodyUriSpec requestBodyUriSpec;
	private ServiceHandlerFactory serviceHandlerFactory;
	
	private ServiceSpec serviceSpec;
	private String resolverTypeName;

	
	public ServiceDelegatorImpl(RequestBodyUriSpec requestBodyUriSpec
			, ServiceHandlerFactory serviceHandlerFactory
			, ServiceSpec serviceSpec
			, String resolverTypeName) {
		
		this.requestBodyUriSpec = requestBodyUriSpec;
		this.serviceHandlerFactory = serviceHandlerFactory;
		this.serviceSpec = serviceSpec;
		this.resolverTypeName = resolverTypeName;
	}
	
	public Mono<ServiceResult<Sr>> run(HttpHeaders clientHeaders, P clientBody) {
		MediaType aggregateType = this.serviceSpec.getAggregateType();
		MediaType serviceRequestType = this.serviceSpec.getServiceRequestType();
		
		ServiceRequestHandler<P, Cq, ?, ?> serviceRequestHandler = this.serviceHandlerFactory.getRequestHandler(aggregateType
				, serviceRequestType
				, this.serviceSpec.getServiceRequestTransformSpec());

		RequestBodySpec requestBodySpec = serviceRequestHandler.handleHeader(this.requestBodyUriSpec, this.serviceSpec);
		RequestHeadersSpec<?> requestHeaderSpec = serviceRequestHandler.resolveBody(clientBody, requestBodySpec, this.serviceSpec);
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
					
					ServiceResponseHandler<Sr, ?, ?> serviceResponseHandler =
							this.serviceHandlerFactory.getResponseHandler(backendContentType
									, clientResponseType
									, this.resolverTypeName
									, this.serviceSpec.getServiceResponseTransformSpec(backendContentType));
					
					return serviceResponseHandler.resolve(responseEntity, this.serviceSpec);
				})
				.onErrorResume(WebClientResponseException.class, ex -> {
					this.log.debug("BACKEND SERVICE ERROR RESPONSE STATUS: " + this.serviceSpec.createBackendURI() + "(" + ex.getRawStatusCode() + ")");
					
					return Mono.just(new ServiceResult<Sr>(this.serviceSpec, ex));
				});
		
		return serviceResult;
	}


}
