package dagateway.server.filter;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import dagateway.api.context.RouteRequestContext;
import dagateway.api.context.RouteRequestContext.ServiceSpec;
import dagateway.api.context.route.EndpointType;
import dagateway.api.handler.ContentHandlerFactory;
import dagateway.api.inserter.BodyInserterBuilderFactory;
import dagateway.api.resolver.ws.WebSocketMessageResolver;
import dagateway.api.resolver.ws.WebSocketMessageResolverFactory;
import dagateway.api.service.ServiceExceptionResolver;
import dagateway.api.utils.ServerWebExchangeUtils;
import dagateway.server.controller.WebSocket2HTTPHandler;
import dagateway.server.controller.WebSocket2WebSocketHandler;
import reactor.core.publisher.Mono;



/**
 * 웹소켓 직접 처리.
 * 프로토콜 변환을 위해 다변화 처리 필요....
 * between Frontend Gateway and Backend(HTTP or WebSocket...) 
 * @author chodo
 * @see org.springframework.cloud.gateway.filter.WebsocketRoutingFilter
 */
@Component
public class WebsocketGatewayFilter implements WebFilter, Ordered {
	public static final int WEBSOCKET_GATEWAY_FILTER_ORDER = Ordered.LOWEST_PRECEDENCE;
	private final Logger log = LoggerFactory.getLogger(WebsocketGatewayFilter.class);
	
	@Autowired
	private WebSocketService webSocketService;
	
	@Autowired
	private WebSocketMessageResolverFactory webSocketMessageResolverFactory;
	
	@Autowired
	private ContentHandlerFactory contentHandlerFactory;
	
	@Autowired
	private BodyInserterBuilderFactory bodyInserterBuilderFactory;
	
	@Autowired
	private ServiceExceptionResolver exceptionResolver;
	
	
	public WebsocketGatewayFilter() {
	}
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//		this.log.debug("filter: " + exchange.getRequest().getURI());
		
		ServerHttpRequest req = exchange.getRequest();
		URI reqURI = req.getURI();
		String schema = reqURI.getScheme().toLowerCase();
		String upgrade = req.getHeaders().getUpgrade();
		
		RouteRequestContext routeContext = ServerWebExchangeUtils.getRouteContext(exchange);
		
		if(routeContext != null
				&& "WebSocket".equalsIgnoreCase(upgrade)
				&& ("http".equals(schema) || "https".equals(schema))
				&& this.isAlreadyRouted(exchange) == false) {
			this.setAlreadyRouted(exchange);

			// TODO Support Mutlple ServiceSpec;
			List<RouteRequestContext.ServiceSpec> serviceSpecs = routeContext.getServiceSpecList();
			ServiceSpec serviceSpec = serviceSpecs.get(0);
			
			WebSocketHandler websocketHandler = this.buildHandler(serviceSpec);
			
			return this.webSocketService.handleRequest(exchange, websocketHandler);
		}

		return chain.filter(exchange);
	}
	
	private <T, V> WebSocketHandler buildHandler(ServiceSpec serviceSpec) {
		WebSocketHandler websocketHandler = null;
		
		if(serviceSpec.getEndpointType() == EndpointType.WEBSOCKET) {
			MediaType aggregateType = serviceSpec.getAggregateType();
			MediaType backendType = serviceSpec.getServiceRequestType();
			
			WebSocketMessageResolver<T> clientResolver = this.webSocketMessageResolverFactory.getMessageResolver(aggregateType);
			WebSocketMessageResolver<V> backendResolver = this.webSocketMessageResolverFactory.getMessageResolver(backendType);
			
			websocketHandler = new WebSocket2WebSocketHandler<T, V>(
					this.contentHandlerFactory
					, serviceSpec
					, clientResolver
					, backendResolver
					);
			
		} else if(serviceSpec.getEndpointType() == EndpointType.HTTP) {
			MediaType aggregateType = serviceSpec.getAggregateType();
			
			WebSocketMessageResolver<T> clientResolver = this.webSocketMessageResolverFactory.getMessageResolver(aggregateType);
			
			websocketHandler = new WebSocket2HTTPHandler<T>(
					this.contentHandlerFactory
					, this.bodyInserterBuilderFactory
					, this.exceptionResolver
					, serviceSpec
					, clientResolver
					);
		}
		
		return websocketHandler;
	}

	@Override
	public int getOrder() {
		return WebsocketGatewayFilter.WEBSOCKET_GATEWAY_FILTER_ORDER;
	}

	private void setAlreadyRouted(ServerWebExchange exchange) {
		exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ALREADY_ROUTED_ATTRIBUTE, true);
	}
	
	private boolean isAlreadyRouted(ServerWebExchange exchange) {
		return exchange.getAttributeOrDefault(ServerWebExchangeUtils.GATEWAY_ALREADY_ROUTED_ATTRIBUTE, false);
	}
	

}
