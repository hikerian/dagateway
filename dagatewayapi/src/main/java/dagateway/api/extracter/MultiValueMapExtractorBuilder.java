package dagateway.api.extracter;

import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;

import reactor.core.publisher.Mono;


public class MultiValueMapExtractorBuilder extends AbstractBodyExtractorBuilder<Mono<MultiValueMap<String, String>>, ReactiveHttpInputMessage> {
	
	
	public MultiValueMapExtractorBuilder() {
		
	}

	@Override
	public BodyExtractor<Mono<MultiValueMap<String, String>>, ReactiveHttpInputMessage> getBodyExtractor() {
		return BodyExtractors.toFormData();
	}

}
