package dagateway.api.context;

import org.springframework.web.server.ServerWebExchange;

/**
 * @see org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory
 * @see org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory
 * @see org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory
 * @see org.springframework.cloud.gateway.support.ServerWebExchangeUtils
 * @see org.springframework.web.util.pattern.PathPatternParser
 * 
 * @author chodo
 *
 */
public interface RoutePredicate {
	public void setValues(String... values);
	public boolean test(ServerWebExchange serverWebExchange);
	
}
