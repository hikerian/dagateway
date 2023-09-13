package dagateway.api.inserter;

import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;

import reactor.core.publisher.Mono;


public class MonoStringInserterBuilder extends AbstractBodyInserterBuilder<Mono<String>, ReactiveHttpOutputMessage> {

	
	public MonoStringInserterBuilder() {
	}

	@Override
	public BodyInserter<?, ReactiveHttpOutputMessage> getBodyInserter(Mono<String> data) {
		return BodyInserters.fromProducer(data, String.class);
	}

}
