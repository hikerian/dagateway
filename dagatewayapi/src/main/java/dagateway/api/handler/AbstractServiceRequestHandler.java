package dagateway.api.handler;

import java.util.Map;

import org.reactivestreams.Publisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import dagateway.api.context.RouteContext;
import dagateway.api.context.RouteContext.HeaderSpec;
import dagateway.api.transform.DataTransformer;
import dagateway.api.utils.Utils;



public abstract class AbstractServiceRequestHandler<P extends Publisher<Cq>, Cq, T, V> implements ServiceRequestHandler<P, Cq, T, V> {
	protected DataTransformer<T, V> transformer;
	protected MediaType clientContentType;
	protected MediaType backendContentType;
	
	
	protected AbstractServiceRequestHandler() {
	}
	
	public void init(DataTransformer<T, V> transformer, MediaType clientContentType, MediaType backendContentType) {
		this.transformer = transformer;
		this.clientContentType = clientContentType;
		this.backendContentType = backendContentType;
	}

	@Override
	public final RequestBodySpec handleHeader(RequestBodyUriSpec requestUriSpec, RouteContext.ServiceSpec serviceSpec) {
		HttpHeaders clientRequestHeaders = serviceSpec.getClientRequestHeaders();
		HeaderSpec headerSpec = serviceSpec.getServiceRequestHeaders();
		
		Map<String, String> variables = serviceSpec.getVariables();
		
		return requestUriSpec.headers(headers -> {
			Utils.filterHeader(clientRequestHeaders, headers, headerSpec, variables);
			headers.setContentType(this.backendContentType);
		});
	}
	
	@Override
	public abstract RequestHeadersSpec<?> resolveBody(P requestBody, RequestBodySpec requestBodySpec, RouteContext.ServiceSpec serviceSpec);


}
