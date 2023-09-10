package dagateway.server.resolver.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteContext;
import dagateway.api.handler.ServiceResponseHandler;
import dagateway.api.resolver.SingleBackendResponseResolver;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Mono;



public class DynamicResponseResolver<Sr> extends SingleBackendResponseResolver<Sr> {
	private final Logger log = LoggerFactory.getLogger(DynamicResponseResolver.class);
	
	
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
			
			ServiceResponseHandler<Sr, ?, ?> serviceResponseHandler = result.getResponseHandler();
			
			// header 처리
			this.buildHeader(bodyBuilder, backendHeaders, routeContext, responseContentType);
			
			// body 처리
			Sr backendBody = result.getBody();
			return serviceResponseHandler.buildBody(bodyBuilder, backendBody);
		});
	}


}
