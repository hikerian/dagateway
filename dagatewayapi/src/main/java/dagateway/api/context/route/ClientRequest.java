package dagateway.api.context.route;

import org.springframework.http.MediaType;


/**
 * @author Dong-il Cho
 */
public class ClientRequest {
	private MediaType aggregateType;
	
	
	public ClientRequest() {
	}

	public MediaType getAggregateType() {
		return this.aggregateType;
	}

	public void setAggregateType(MediaType aggregateType) {
		this.aggregateType = aggregateType;
	}

	@Override
	public String toString() {
		return "ClientRequest [aggregateType=" + aggregateType + "]";
	}


}
