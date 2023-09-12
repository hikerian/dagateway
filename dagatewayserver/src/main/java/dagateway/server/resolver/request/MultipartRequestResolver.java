package dagateway.server.resolver.request;

import org.springframework.http.codec.multipart.Part;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;

import dagateway.api.resolver.SingleRequestDataResolver;
import reactor.core.publisher.Mono;



public class MultipartRequestResolver extends SingleRequestDataResolver<MultiValueMap<String, Part>> {
	
	
	public MultipartRequestResolver() {
	}
	
	@Override
	public Mono<MultiValueMap<String, Part>> doResolve(ServerRequest serverRequest) {
		return serverRequest.body(BodyExtractors.toMultipartData());
	}

	@Override
	public MultiValueMap<String, Part> emptyValue() {
		return new LinkedMultiValueMap<>();
	}

}
