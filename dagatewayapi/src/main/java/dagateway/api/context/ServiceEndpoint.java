package dagateway.api.context;


public class ServiceEndpoint {
	private EndpointType type;
	private String uri;
	private String path;
	
	
	public ServiceEndpoint() {
	}

	public EndpointType getType() {
		return this.type;
	}

	public void setType(EndpointType type) {
		this.type = type;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "ServiceEndpoint [type=" + type + ", uri=" + uri + ", path=" + path + "]";
	}


}
