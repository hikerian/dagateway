package dagateway.api.context;

import org.springframework.http.HttpMethod;


public class ServiceRequest {
	private HttpMethod method;
	private HeaderProperties header;
	private ServiceRequestBody body;
	
	
	public ServiceRequest() {
	}

	public HttpMethod getMethod() {
		return this.method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public HeaderProperties getHeader() {
		return this.header;
	}

	public void setHeader(HeaderProperties header) {
		this.header = header;
	}

	public ServiceRequestBody getBody() {
		return this.body;
	}

	public void setBody(ServiceRequestBody body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "ServiceRequest [header=" + header + ", body=" + body + "]";
	}

}
