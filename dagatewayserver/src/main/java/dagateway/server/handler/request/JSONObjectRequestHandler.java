package dagateway.server.handler.request;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import dagateway.api.context.RouteContext;
import dagateway.api.handler.AbstractServiceRequestHandler;
import net.minidev.json.JSONObject;
import reactor.core.publisher.Mono;


public class JSONObjectRequestHandler extends AbstractServiceRequestHandler<Mono<JSONObject>, JSONObject, JSONObject, JSONObject> {
	
	
	public JSONObjectRequestHandler() {
		
	}

	@Override
	public RequestHeadersSpec<?> resolveBody(Mono<JSONObject> requestBody, RequestBodySpec requestBodySpec, RouteContext.ServiceSpec serviceSpec) {
		// transform
		Mono<JSONObject> transformed = requestBody.map(jsonObject -> this.transformer.transform(jsonObject));
		
		return requestBodySpec.body(BodyInserters.fromProducer(transformed.map(jsonObject -> jsonObject.toJSONString()), String.class));
	}

}
