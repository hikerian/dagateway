package dagateway.api.handler;

import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import dagateway.api.context.RouteContext;



// map source content type -> target content type
public interface ServiceRequestHandler<P extends Publisher<Cq>, Cq, T, V> {
	public RequestBodySpec handleHeader(RequestBodyUriSpec requestUriSpec, RouteContext.ServiceSpec serviceSpec);
	public RequestHeadersSpec<?> resolveBody(P requestBody, RequestBodySpec requestBodySpec, RouteContext.ServiceSpec serviceSpec);

}
