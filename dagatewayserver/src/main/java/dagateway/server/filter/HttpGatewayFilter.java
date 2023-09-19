package dagateway.server.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import dagateway.api.context.GatewayContext;
import dagateway.api.context.GatewayRouteContext;
import dagateway.api.context.RouteRequestContext;
import dagateway.api.context.route.GatewayRoute;
import dagateway.api.utils.ServerWebExchangeUtils;
import reactor.core.publisher.Mono;



@Component
public class HttpGatewayFilter implements WebFilter, Ordered {
	private final Logger log = LoggerFactory.getLogger(HttpGatewayFilter.class);
	
	@Autowired
	private GatewayContext gatewayContext;
	
	
	public HttpGatewayFilter() {
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//		this.log.debug("filter: " + exchange.getRequest().getURI());
		
		GatewayRouteContext gatewayRoute = this.gatewayContext.getRoute(exchange);
		if(gatewayRoute == null) {
			return chain.filter(exchange);
		}
		
		RouteRequestContext routeContext = new RouteRequestContext(exchange, gatewayRoute);
		ServerWebExchangeUtils.putRouteContext(exchange, routeContext);
		
//		this.log.debug("RouteContext putted: " + routeContext);
		
		return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return WebsocketGatewayFilter.WEBSOCKET_GATEWAY_FILTER_ORDER - 1;
	}

}
