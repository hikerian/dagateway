package dagateway.api.context.predicate;

import org.springframework.web.server.ServerWebExchange;


/**
 * @author Dong-il Cho
 *
 */
public interface RoutePredicate {
	public void setValues(String... values);
	public boolean test(ServerWebExchange serverWebExchange);
	
}
