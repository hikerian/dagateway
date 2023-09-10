package dagateway.server.resolver.request;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.server.ServerRequest;

import dagateway.api.context.RouteContext;
import dagateway.api.resolver.SingleRequestDataResolver;
import net.minidev.json.JSONObject;
import reactor.core.publisher.Mono;



public class JSONObjectRequestResolver extends SingleRequestDataResolver<JSONObject> {
	
	
	public JSONObjectRequestResolver() {
	}
	
	@Override
	public Mono<JSONObject> doResolve(RouteContext routeContext, ServerRequest serverRequest) {
		ParameterizedTypeReference<JSONObject> jsonType = new ParameterizedTypeReference<>() {};
		
		return serverRequest.bodyToMono(jsonType);
	}

	@Override
	public JSONObject emptyValue() {
		return new JSONObject();
	}

}
