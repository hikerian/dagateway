package dagateway.api.inserter;

import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;

import reactor.core.publisher.Mono;


public class MultiValueMapInserterBuilder extends AbstractBodyInserterBuilder<Mono<MultiValueMap<String, String>>, ReactiveHttpOutputMessage> {
	
	
	public MultiValueMapInserterBuilder() {
	}

	@Override
	public BodyInserter<?, ReactiveHttpOutputMessage> getBodyInserter(Mono<MultiValueMap<String, String>> data) {
		return BodyInserters.fromProducer(data, MultiValueMap.class);
	}

}
