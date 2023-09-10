package dagateway.api.context;


public class ServiceTarget {
	private String name;
	private ServiceEndpoint endpoint;
	private ServiceRequest request;
	private ServiceResponse response;
	
	
	public ServiceTarget() {
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public ServiceEndpoint getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(ServiceEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	public ServiceRequest getRequest() {
		return this.request;
	}

	public void setRequest(ServiceRequest request) {
		this.request = request;
	}

	public ServiceResponse getResponse() {
		return this.response;
	}

	public void setResponse(ServiceResponse response) {
		this.response = response;
	}

	@Override
	public String toString() {
		return "ServiceTarget [name=" + name
				+ ", endpoint=" + endpoint
				+ ", request=" + request
				+ ", response=" + response + "]";
	}



}
