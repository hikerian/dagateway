package dagateway.api.context.route;

import java.util.List;


/**
 * @author Dong-il Cho
 */
public class ServiceResponseBody {
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
