package dagateway.api.extracter;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;

import net.minidev.json.JSONObject;
import reactor.core.publisher.Mono;


public class JSONObjectExtractorBuilder extends AbstractBodyExtractorBuilder<Mono<JSONObject>, ReactiveHttpInputMessage> {
	
	
	public JSONObjectExtractorBuilder() {
	}

	@Override
	public BodyExtractor<Mono<JSONObject>, ReactiveHttpInputMessage> getBodyExtractor() {
		ParameterizedTypeReference<JSONObject> jsonType = new ParameterizedTypeReference<>() {};
		
		return BodyExtractors.toMono(jsonType);
	}

}
