package dagateway.api.context.route;


/**
 * @author Dong-il Cho
 */
public class ServiceRequestBody {
	private TransformRule transform;
	
	
	public ServiceRequestBody() {
	}

	public TransformRule getTransform() {
		return this.transform;
	}

	public void setTransform(TransformRule transform) {
		this.transform = transform;
	}

	@Override
	public String toString() {
		return "ServiceRequestBody [transform=" + transform + "]";
	}



}
