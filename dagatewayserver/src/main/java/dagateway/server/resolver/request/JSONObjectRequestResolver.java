package dagateway.server.resolver.request;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.server.ServerRequest;

import dagateway.api.resolver.http.SingleRequestDataResolver;
import net.minidev.json.JSONObject;
import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 */
public class JSONObjectRequestResolver extends SingleRequestDataResolver<JSONObject> {
	
	
	public JSONObjectRequestResolver() {
	}
	
	@Override
	public Mono<JSONObject> doResolve(ServerRequest serverRequest) {
		ParameterizedTypeReference<JSONObject> jsonType = new ParameterizedTypeReference<>() {};
		
		return serverRequest.bodyToMono(jsonType);
	}

	@Override
	public JSONObject emptyValue() {
		return new JSONObject();
	}
	


}
