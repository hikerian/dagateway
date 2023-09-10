package dagateway.api.context;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;


public class GatewayContext {
	private final Logger log = LoggerFactory.getLogger(GatewayContext.class);
	
	private List<GatewayRoute> routes = new CopyOnWriteArrayList<>();
	
	
	public GatewayContext() {
	}
	
	public void addRoutes(GatewayRoutes routes) {
		List<GatewayRoute> routeList = routes.getRoutes();
		if(routeList != null && routeList.size() > 0) {
			this.routes.addAll(routeList);
		}
	}
	
	public GatewayRoute getRoute(ServerWebExchange serverWebExchange) {
		GatewayRoute gatewayRoute = null;
		
		routeLoop: for(GatewayRoute route : this.routes) {
			List<RoutePredicate> predicates = route.getPredicates();
			for(RoutePredicate predicate : predicates) {
				if(predicate.test(serverWebExchange) == false) {
					continue routeLoop;
				}
				this.log.debug("Matched Predicate: " + predicate);
			}
			gatewayRoute = route;
			break;
		}
		
		return gatewayRoute;
	}


}
