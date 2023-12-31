package dagateway.api.context.route;


/**
 * @author Dong-il Cho
 */
public class ServiceResponse {
	private ServiceResponseBody body;
	
	
	public ServiceResponse() {
	}

	public ServiceResponseBody getBody() {
		return this.body;
	}

	public void setBody(ServiceResponseBody body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "ServiceResponse [body=" + body + "]";
	}

}
