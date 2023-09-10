package dagateway.api.context;

import java.util.ArrayList;
import java.util.List;


public class GatewayRoutes {
	private List<GatewayRoute> routes = new ArrayList<>();
	
	
	public GatewayRoutes() {
	}

	public List<GatewayRoute> getRoutes() {
		return this.routes;
	}

	public void setRoutes(List<GatewayRoute> routes) {
		this.routes = routes;
	}

	@Override
	public String toString() {
		return "Routes [routes=" + routes + "]";
	}

}
