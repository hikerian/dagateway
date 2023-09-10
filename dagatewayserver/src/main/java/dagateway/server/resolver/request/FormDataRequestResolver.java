package dagateway.server.resolver.request;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;

import dagateway.api.context.RouteContext;
import dagateway.api.resolver.SingleRequestDataResolver;
import reactor.core.publisher.Mono;



public class FormDataRequestResolver extends SingleRequestDataResolver<MultiValueMap<String, String>> {
	
	
	public FormDataRequestResolver() {
	}
	
	@Override
	public Mono<MultiValueMap<String, String>> doResolve(RouteContext routeContext, ServerRequest serverRequest) {
		return serverRequest.body(BodyExtractors.toFormData());
	}

	@Override
	public MultiValueMap<String, String> emptyValue() {
		return new LinkedMultiValueMap<>();
	}


}
