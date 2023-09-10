package dagateway.api.context;


public enum EndpointType {
	HTTP("HTTP"),
	WEBSOCKET("WEBSOCKET");
	
	private String value;
	
	EndpointType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
}
