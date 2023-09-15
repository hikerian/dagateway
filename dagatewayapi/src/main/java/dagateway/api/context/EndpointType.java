package dagateway.api.context;


/**
 * @author Dong-il Cho
 */
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
