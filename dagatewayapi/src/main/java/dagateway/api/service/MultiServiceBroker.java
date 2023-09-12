package dagateway.api.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteContext;
import dagateway.api.resolver.ClientRequestResolver;
import dagateway.api.resolver.ClientResponseResolver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public class MultiServiceBroker<Cq, Sr> implements ServiceBroker<Mono<Cq>, Cq, Sr> {
	private final Logger log = LoggerFactory.getLogger(MultiServiceBroker.class);
	
	private RouteContext routeContext;
	private ClientRequestResolver<Mono<Cq>, Cq> requestResolver;
	private ClientResponseResolver<Flux<ServiceResult<Sr>>, Sr> responseResolver;

	private List<ServiceDelegator<Mono<Cq>, Cq, Sr>> serviceDelegatorList;

	
	public MultiServiceBroker(RouteContext routeContext, ClientRequestResolver<Mono<Cq>, Cq> requestResolver, ClientResponseResolver<Flux<ServiceResult<Sr>>, Sr> responseResolver) {
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

		Flux<ServiceResult<Sr>> serviceResults = monoBody
				.flatMapMany(body -> {
			List<Mono<ServiceResult<Sr>>> serviceResultList = new ArrayList<>();
			Mono<Cq> cqMono = Mono.just(body);
			for(ServiceDelegator<Mono<Cq>, Cq, Sr> serviceDelegator : this.serviceDelegatorList) {
				Mono<ServiceResult<Sr>> serviceResult = serviceDelegator.run(requestHeaders, cqMono);
				serviceResultList.add(serviceResult);
			}
			
			return Flux.concat(serviceResultList);
		});
		
		return serviceResults;
	}

	public void addServiceDelegator(ServiceDelegator<Mono<Cq>, Cq, Sr> serviceDelegator) {
		if(this.serviceDelegatorList == null) {
			this.serviceDelegatorList = new ArrayList<>();
		}
		this.serviceDelegatorList.add(serviceDelegator);
	}
	


}
