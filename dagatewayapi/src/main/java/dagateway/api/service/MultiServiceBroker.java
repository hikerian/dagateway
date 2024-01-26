package dagateway.api.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteRequestContext;
import dagateway.api.resolver.http.ClientRequestResolver;
import dagateway.api.resolver.http.ClientResponseResolver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;



/**
 * @author Dong-il Cho
 */
public class MultiServiceBroker<Cq, Sr> implements ServiceBroker<Mono<Cq>, Cq, Sr> {
	private final Logger log = LoggerFactory.getLogger(MultiServiceBroker.class);
	
	private RouteRequestContext routeContext;
	private ClientRequestResolver<Mono<Cq>, Cq> requestResolver;
	private ClientResponseResolver<Flux<ServiceResult<Sr>>, Sr> responseResolver;

	private List<ServiceDelegator<Mono<Cq>, Cq, Sr>> serviceDelegatorList;

	
	public MultiServiceBroker(RouteRequestContext routeContext, ClientRequestResolver<Mono<Cq>, Cq> requestResolver, ClientResponseResolver<Flux<ServiceResult<Sr>>, Sr> responseResolver) {
		this.routeContext = routeContext;
		this.requestResolver = requestResolver;
		this.responseResolver = responseResolver;
	}
	
	public Mono<ServerResponse> run(ServerRequest serverRequest) {
		this.log.debug("run");
		
		Flux<ServiceResult<Sr>> serviceResults = this.runServices(serverRequest);
		Mono<ServerResponse> serverResponse = this.responseResolver.resolve(this.routeContext, serviceResults);

		return serverResponse;
	}
	
	private Flux<ServiceResult<Sr>> runServices(ServerRequest serverRequest) {
		this.log.debug("runServices");
		
		HttpHeaders requestHeaders = this.routeContext.getRequestHeaders();
		// Divided Data is not support
		Mono<Cq> monoBody = this.requestResolver.resolve(serverRequest);
		
		Flux<ServiceResult<Sr>> serviceResultFlux = monoBody.flatMapMany(body -> {
			Flux<ServiceResult<Sr>> serviceResults = Flux.fromIterable(this.serviceDelegatorList)
					.flatMap((delegator) -> {
						Mono<ServiceResult<Sr>> serviceResult = delegator.run(requestHeaders, Mono.just(body));
						return serviceResult;
					}).subscribeOn(Schedulers.parallel());

			return serviceResults;
		});
		
		return serviceResultFlux;
	}

	public void addServiceDelegator(ServiceDelegator<Mono<Cq>, Cq, Sr> serviceDelegator) {
		if(this.serviceDelegatorList == null) {
			this.serviceDelegatorList = new ArrayList<>();
		}
		this.serviceDelegatorList.add(serviceDelegator);
	}
	


}
