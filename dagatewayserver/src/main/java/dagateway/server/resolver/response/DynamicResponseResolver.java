package dagateway.server.resolver.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteContext;
import dagateway.api.inserter.BodyInserterBuilderFactory;
import dagateway.api.resolver.http.SingleBackendResponseResolver;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Mono;



public class DynamicResponseResolver<Sr> extends SingleBackendResponseResolver<Sr> {
	private final Logger log = LoggerFactory.getLogger(DynamicResponseResolver.class);
	
	@Autowired
	private BodyInserterBuilderFactory bodyInserterBuilderFactory;
	
	
	public DynamicResponseResolver() {
	}
	
	@Override
	public String getTypeArgument() {
		return "*";
	}
	
	@Override
	public Mono<ServerResponse> resolve(RouteContext routeContext, Mono<ServiceResult<Sr>> serviceResult) {
		this.log.debug("RESPONSE CONTENTTYPE: " + this.contentType);
		
		return serviceResult.flatMap(result -> {
			HttpHeaders backendHeaders = result.getHeaders();
			
			ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok();
			
			MediaType backendContentType = backendHeaders.getContentType();
			backendContentType = backendContentType == null ? routeContext.getResponseType() : backendContentType;
			final MediaType responseContentType = backendContentType;
			
			// header 처리
			this.buildHeader(bodyBuilder, backendHeaders, routeContext, responseContentType);
			
			// body 처리
			Sr backendBody = result.getBody();
			String returnTypeName = result.getBodyTypeName();
			
			return bodyBuilder.body(this.bodyInserterBuilderFactory.getBodyInserter(returnTypeName, backendBody));
		});
	}
	
	public Sr resolveBody(RouteContext routeContext, ServiceResult<Sr> serviceResult) {
		this.log.debug("RESPONSE CONTENTTYPE: " + this.contentType);
		
		return serviceResult.getBody();
	}


}
