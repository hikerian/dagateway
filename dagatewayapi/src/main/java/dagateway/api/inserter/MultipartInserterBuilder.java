package dagateway.api.inserter;

import org.springframework.http.HttpEntity;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;

import reactor.core.publisher.Mono;


public class MultipartInserterBuilder
	extends AbstractBodyInserterBuilder<Mono<MultiValueMap<String, HttpEntity<?>>>, ReactiveHttpOutputMessage> {
	
	
	public MultipartInserterBuilder() {
	}

	@Override
	public BodyInserter<?, ReactiveHttpOutputMessage> getBodyInserter(Mono<MultiValueMap<String, HttpEntity<?>>> data) {
		return BodyInserters.fromProducer(data, MultiValueMap.class);
	}

}
