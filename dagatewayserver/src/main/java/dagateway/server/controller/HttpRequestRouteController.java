package dagateway.server.controller;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteRequestContext;
import dagateway.api.service.ServiceBroker;
import dagateway.api.service.ServiceBrokerBuilder;
import dagateway.api.utils.ServerWebExchangeUtils;
import reactor.core.publisher.Mono;


@Component
public class HttpRequestRouteController {
	private final Logger log = LoggerFactory.getLogger(HttpRequestRouteController.class);
	
	private final ServiceBrokerBuilder serviceBrokerBuilder;
	
	
	public HttpRequestRouteController(ServiceBrokerBuilder serviceBrokerBuilder) {
		this.serviceBrokerBuilder = serviceBrokerBuilder;
	}
	
	public <P extends Publisher<Cq>, Cq, Sr> Mono<ServerResponse> service(ServerRequest serverRequest) {
		this.log.debug("build ServiceTasks");
		RouteRequestContext routeContext = ServerWebExchangeUtils.getRouteContext(serverRequest);
		ServiceBroker<P, Cq, Sr> serviceBroker = this.serviceBrokerBuilder.build(routeContext);
		
		this.log.debug("run ServiceTasks");
		Mono<ServerResponse> serverResponse = serviceBroker.run(serverRequest);
		
		this.log.debug("return ServerResponse");
		return serverResponse;
	}


}
