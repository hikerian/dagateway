package dagateway.api.context.route;

import java.util.List;

import dagateway.api.context.predicate.RoutePredicate;


/**
 * Filter Usage
 * @author Dong-il Cho
 *
 */
public class GatewayRoute {
	private List<RoutePredicate> predicates;
	private ClientRequest clientRequest;
	private ClientResponse clientResponse;
	private List<ServiceTarget> serviceTargets;
	
	
	public GatewayRoute() {
	}

	public List<RoutePredicate> getPredicates() {
		return this.predicates;
	}

	public void setPredicates(List<RoutePredicate> predicates) {
		this.predicates = predicates;
	}

	public ClientRequest getClientRequest() {
		return this.clientRequest;
	}

	public void setClientRequest(ClientRequest clientRequest) {
		this.clientRequest = clientRequest;
	}

	public ClientResponse getClientResponse() {
		return this.clientResponse;
	}

	public void setClientResponse(ClientResponse clientResponse) {
		this.clientResponse = clientResponse;
	}

	public List<ServiceTarget> getServiceTargets() {
		return this.serviceTargets;
	}

	public void setServiceTargets(List<ServiceTarget> serviceTargets) {
		this.serviceTargets = serviceTargets;
	}

	@Override
	public String toString() {
		return "GatewayRoute [predicates=" + predicates
				+ ", clientRequest=" + clientRequest
				+ ", clientResponse=" + clientResponse
				+ ", serviceTargets=" + serviceTargets + "]";
	}


}
