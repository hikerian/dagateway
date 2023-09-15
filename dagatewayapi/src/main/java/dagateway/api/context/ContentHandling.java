package dagateway.api.context;


/**
 * @author Dong-il Cho
 */
public enum ContentHandling {
	PASSTHROUGH("passthrough"),
	COMPOSE("compose");
	
	private String value;
	
	ContentHandling(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}

}
