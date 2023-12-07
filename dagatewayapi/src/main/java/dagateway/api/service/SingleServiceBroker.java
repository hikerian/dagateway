package dagateway.api.service;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteRequestContext;
import dagateway.api.resolver.http.ClientRequestResolver;
import dagateway.api.resolver.http.ClientResponseResolver;
import reactor.core.publisher.Mono;



public class SingleServiceBroker<P extends Publisher<Cq>, Cq, Sr> implements ServiceBroker<P, Cq, Sr> {
	private final Logger log = LoggerFactory.getLogger(SingleServiceBroker.class);
	
	private RouteRequestContext routeContext;
	private ClientRequestResolver<P, Cq> requestResolver;
	private ClientResponseResolver<Mono<ServiceResult<Sr>>, Sr> responseResolver;
	
	private ServiceDelegator<P, Cq, Sr> serviceDelegator;
	
	
	public SingleServiceBroker(RouteRequestContext routeContext, ClientRequestResolver<P, Cq> requestResolver, ClientResponseResolver<Mono<ServiceResult<Sr>>, Sr> responseResolver) {
		this.routeContext = routeContext;
		this.requestResolver = requestResolver;
		this.responseResolver = responseResolver;
	}

	@Override
	public Mono<ServerResponse> run(ServerRequest serverRequest) {
		this.log.debug("run");
		
		HttpHeaders requestHeaders = this.routeContext.getRequestHeaders();
		P aggregateBody = this.requestResolver.resolve(serverRequest);
		
		Mono<ServiceResult<Sr>> serviceResult = this.serviceDelegator.run(requestHeaders, aggregateBody);
		Mono<ServerResponse> serverResponse = this.responseResolver.resolve(this.routeContext, serviceResult);
		
		return serverResponse;
	}
	
	public void setServiceDelegator(ServiceDelegator<P, Cq, Sr> serviceDelegator) {
		this.serviceDelegator = serviceDelegator;
	}


}
