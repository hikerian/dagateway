package dagateway.api.inserter.impl;

import org.springframework.http.HttpEntity;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;

import dagateway.api.inserter.AbstractBodyInserterBuilder;
import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 */
public class MultipartInserterBuilder
	extends AbstractBodyInserterBuilder<Mono<MultiValueMap<String, HttpEntity<?>>>, ReactiveHttpOutputMessage> {
	
	
	public MultipartInserterBuilder() {
	}

	@Override
	public BodyInserter<?, ReactiveHttpOutputMessage> getBodyInserter(Mono<MultiValueMap<String, HttpEntity<?>>> data) {
		return BodyInserters.fromProducer(data, MultiValueMap.class);
	}
	
	@Override
	public String supportType() {
		return "reactor.core.publisher.Mono<org.springframework.util.MultiValueMap<java.lang.String, org.springframework.http.HttpEntity<?>>>";
	}

}
