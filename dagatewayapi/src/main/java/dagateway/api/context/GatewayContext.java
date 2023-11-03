package dagateway.api.context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.server.ServerWebExchange;

import dagateway.api.context.predicate.RoutePredicate;
import dagateway.api.context.route.GatewayRoute;
import dagateway.api.context.route.GatewayRoutes;


/**
 * @author Dong-il Cho
 */
public class GatewayContext {
	private List<GatewayRouteContext> routes = new CopyOnWriteArrayList<>();
	private Map<String, BackendServer> backends = new ConcurrentHashMap<>();
	
	
	public GatewayContext() {
	}
	
	public void addRoutes(GatewayRoutes routes) {
		List<GatewayRoute> routeList = routes.getRoutes();
		if(routeList != null && routeList.size() > 0) {
			for(GatewayRoute gatewayRoute : routeList) {
				this.routes.add(new GatewayRouteContext(gatewayRoute));
			}
		}
	}
	
	public GatewayRouteContext getRoute(ServerWebExchange serverWebExchange) {
		GatewayRouteContext gatewayRoute = null;
		
		routeLoop: for(GatewayRouteContext route : this.routes) {
			List<RoutePredicate> predicates = route.getPredicates();
			for(RoutePredicate predicate : predicates) {
				if(predicate.test(serverWebExchange) == false) {
					continue routeLoop;
				}
			}
			gatewayRoute = route;
			break;
		}
		
		return gatewayRoute;
	}
	
	public void addBackends(BackendServers backendServers) {
		List<BackendServer> backendServerList = backendServers.getBackends();
		if(backendServerList != null && backendServerList.size() > 0) {
			for(BackendServer backendServer : backendServerList) {
				String serverName = backendServer.getName();
				this.backends.put(serverName, backendServer);
			}
		}
	}
	
	public BackendServer getBackend(String serverName) {
		return this.backends.get(serverName);
	}


}
