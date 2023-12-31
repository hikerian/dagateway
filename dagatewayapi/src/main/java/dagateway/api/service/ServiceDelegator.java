package dagateway.api.service;

import org.reactivestreams.Publisher;
import org.springframework.http.HttpHeaders;

import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 */
public interface ServiceDelegator<P extends Publisher<Cq>, Cq, Sr> {
	public <R> Mono<ServiceResult<Sr>> run(HttpHeaders clientHeaders, P clientBody);
}
