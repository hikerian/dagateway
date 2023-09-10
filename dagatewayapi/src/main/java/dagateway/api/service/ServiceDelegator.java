package dagateway.api.service;

import org.reactivestreams.Publisher;
import org.springframework.http.HttpHeaders;

import reactor.core.publisher.Mono;


public interface ServiceDelegator<P extends Publisher<Cq>, Cq, Sr> {
	public Mono<ServiceResult<Sr>> run(HttpHeaders clientHeaders, P clientBody);
}
