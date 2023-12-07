package dagateway.api.inserter.impl;

import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;

import dagateway.api.inserter.AbstractBodyInserterBuilder;
import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 */
public class MonoStringInserterBuilder extends AbstractBodyInserterBuilder<Mono<String>, ReactiveHttpOutputMessage> {

	
	public MonoStringInserterBuilder() {
	}

	@Override
	public BodyInserter<?, ReactiveHttpOutputMessage> getBodyInserter(Mono<String> data) {
		return BodyInserters.fromProducer(data, String.class);
	}

	@Override
	public String supportType() {
		return "reactor.core.publisher.Mono<java.lang.String>";
	}
	
}
