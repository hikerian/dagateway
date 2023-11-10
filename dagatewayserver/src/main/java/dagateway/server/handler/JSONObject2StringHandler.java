package dagateway.server.handler;

import dagateway.api.handler.AbstractContentHandler;
import net.minidev.json.JSONObject;
import reactor.core.publisher.Mono;



public class JSONObject2StringHandler extends AbstractContentHandler<Mono<JSONObject>, JSONObject, JSONObject, JSONObject, Mono<String>> {
	
	
	public JSONObject2StringHandler() {
	}

	@Override
	public Mono<String> handle(Mono<JSONObject> requestBody) {
		// transform
		Mono<JSONObject> transformed = requestBody.map(jsonObject -> this.transformer.transform(jsonObject));

		return transformed.map(jsonObject -> jsonObject.toJSONString());
	}

	@Override
	protected Mono<String> wrapSingle(JSONObject value) {
		return Mono.just(value.toJSONString());
	}

}
