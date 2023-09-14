package dagateway.api.resolver.http;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteContext;
import dagateway.api.context.RouteContext.HeaderSpec;
import dagateway.api.context.RouteContext.ResponseSpec;
import dagateway.api.service.ServiceResult;
import dagateway.api.utils.Utils;
import reactor.core.publisher.Mono;



public abstract class SingleBackendResponseResolver<Sr> extends AbstractClientResponseResolver<Sr, Mono<ServiceResult<Sr>>> {
	public abstract Mono<ServerResponse> resolve(RouteContext routeContext, Mono<ServiceResult<Sr>> serviceResults);

	/*
	 * common methods
	 */
	protected void buildHeader(ServerResponse.BodyBuilder bodyBuilder
			, HttpHeaders backendHeaders
			, RouteContext routeContext
			, MediaType responseContentType) {

		Map<String, String> variables = routeContext.getVariables();
		ResponseSpec responseSpec = routeContext.getResponseSpec();
		HeaderSpec headerSpec = responseSpec.getResponseHeaders();
		
		// header 처리
		bodyBuilder.headers(headers -> {
			Utils.filterHeader(backendHeaders, headers, headerSpec, variables);
			headers.setContentType(responseContentType);
		});
		bodyBuilder.contentType(responseContentType);
	}


}
