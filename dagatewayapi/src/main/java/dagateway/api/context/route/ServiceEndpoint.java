package dagateway.api.context.route;


/**
 * @author Dong-il Cho
 */
public class ServiceEndpoint {
	private EndpointType type;
	private String backendName;
	private String path;
	
	
	public ServiceEndpoint() {
	}

	public EndpointType getType() {
		return this.type;
	}

	public void setType(EndpointType type) {
		this.type = type;
	}

	public String getBackendName() {
		return this.backendName;
	}

	public void setBackendName(String backendName) {
		this.backendName = backendName;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "ServiceEndpoint [type=" + this.type
				+ ", backendName=" + this.backendName
				+ ", path=" + this.path + "]";
	}


}
