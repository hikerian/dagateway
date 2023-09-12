package dagateway.api.extracter;

import org.springframework.http.codec.multipart.Part;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;

import reactor.core.publisher.Mono;


public class MultipartExtractorBuilder extends AbstractBodyExtractorBuilder<Mono<MultiValueMap<String, Part>>, ServerHttpRequest> {
	
	
	public MultipartExtractorBuilder() {
	}

	@Override
	public BodyExtractor<Mono<MultiValueMap<String, Part>>, ServerHttpRequest> getBodyExtractor() {
		return BodyExtractors.toMultipartData();
	}

}
