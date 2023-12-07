package dagateway.server.handler;

import dagateway.api.handler.AbstractContentHandler;
import net.minidev.json.JSONObject;
import reactor.core.publisher.Mono;



public class JSONObject2StringHandler extends AbstractContentHandler<Mono<JSONObject>, JSONObject, JSONObject, JSONObject, Mono<String>> {
	public static final String ARGUMENT_TYPE = "reactor.core.publisher.Mono<net.minidev.json.JSONObject>";
	public static final String RETURN_TYPE = "reactor.core.publisher.Mono<java.lang.String>";
	public static final String TRANS_ARGUMENT_TYPE = "net.minidev.json.JSONObject";
	public static final String TRANS_RETURN_TYPE = "net.minidev.json.JSONObject";
	
	
	public JSONObject2StringHandler() {
	}

	@Override
	public String getArgumentTypeName() {
		return ARGUMENT_TYPE;
	}

	@Override
	public String getReturnTypeName() {
		return RETURN_TYPE;
	}

	@Override
	public String getTransArgumentTypeName() {
		return TRANS_ARGUMENT_TYPE;
	}

	@Override
	public String getTransReturnTypeName() {
		return TRANS_RETURN_TYPE;
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
