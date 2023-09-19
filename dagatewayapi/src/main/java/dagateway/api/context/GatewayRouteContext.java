package dagateway.api.context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dagateway.api.context.predicate.RoutePredicate;
import dagateway.api.context.route.ClientRequest;
import dagateway.api.context.route.ClientResponse;
import dagateway.api.context.route.GatewayRoute;
import dagateway.api.context.route.ServiceTarget;


/**
 * @author Dong-il Cho
 */
public class GatewayRouteContext {
	private GatewayRoute gatewayRoute;
	private Map<String, Object> attributes = new ConcurrentHashMap<>();
	
	
	public GatewayRouteContext(GatewayRoute gatewayRoute) {
		this.gatewayRoute = gatewayRoute;
	}
	
	public List<RoutePredicate> getPredicates() {
		return this.gatewayRoute.getPredicates();
	}
	
	public ClientRequest getClientRequest() {
		return this.gatewayRoute.getClientRequest();
	}
	
	public ClientResponse getClientResponse() {
		return this.gatewayRoute.getClientResponse();
	}
	
	public List<ServiceTarget> getServiceTargets() {
		return this.gatewayRoute.getServiceTargets();
	}
	
	public void setAttribute(String name, Object value) {
		this.attributes.put(name, value);
	}
	
	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}


}
