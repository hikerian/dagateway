package dagateway.api.inserter.impl;

import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;

import dagateway.api.inserter.AbstractBodyInserterBuilder;
import net.minidev.json.JSONObject;
import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 */
public class JSONObjectInserterBuilder extends AbstractBodyInserterBuilder<Mono<JSONObject>, ReactiveHttpOutputMessage> {
	
	
	public JSONObjectInserterBuilder() {
	}

	@Override
	public BodyInserter<?, ReactiveHttpOutputMessage> getBodyInserter(Mono<JSONObject> data) {
		return BodyInserters.fromProducer(data.map(json -> json.toJSONString()), String.class);
	}
	
	@Override
	public String supportType() {
		return "reactor.core.publisher.Mono<net.minidev.json.JSONObject>";
	}

}
