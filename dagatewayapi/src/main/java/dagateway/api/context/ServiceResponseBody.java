package dagateway.api.context;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ServiceResponseBody {
	@JsonProperty("transform")
	private List<TransformRule> transform;
	
	
	public ServiceResponseBody() {
	}

	public List<TransformRule> getTransform() {
		return this.transform;
	}

	public void setTransform(List<TransformRule> transform) {
		this.transform = transform;
	}

	@Override
	public String toString() {
		return "ServiceResponseBody [transform=" + transform + "]";
	}


}
