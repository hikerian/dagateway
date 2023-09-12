package dagateway.api.inserter;

import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;

import reactor.core.publisher.Mono;


public class MultipartInserterBuilder
	extends AbstractBodyInserterBuilder<Mono<MultiValueMap<String, Part>>, ReactiveHttpOutputMessage> {
	
	
	public MultipartInserterBuilder() {
	}

	@Override
	public BodyInserter<?, ReactiveHttpOutputMessage> getBodyInserter(Mono<MultiValueMap<String, Part>> data) {
		return BodyInserters.fromProducer(data, MultiValueMap.class);
	}

}
