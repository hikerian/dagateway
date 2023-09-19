package dagateway.api.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import dagateway.api.context.RouteRequestContext;
import dagateway.api.context.RouteRequestContext.ServiceSpec;


/**
 * TODO Exception 처리
 * @author chodo
 *
 * @param <Sr>
 */
public class ServiceResult<Sr> {
	private RouteRequestContext.ServiceSpec serviceSpec;
	private HttpStatus status;
	
	private WebClientResponseException exception;
	
	private HttpHeaders headers;
	private Sr body;
	private String bodyTypeName;
	

	public ServiceResult(ServiceSpec serviceSpec, WebClientResponseException exception) {
		this.serviceSpec = serviceSpec;
		this.exception = exception;
		this.status = exception.getStatusCode();
		this.headers = exception.getHeaders();
		this.body = null;
		this.bodyTypeName = null;
	}
	
	public ServiceResult(ServiceSpec serviceSpec, HttpStatus status, HttpHeaders headers, Sr body, String bodyTypeName) {
		this.serviceSpec = serviceSpec;
		this.status = status;
		this.headers = headers;
		this.body = body;
		this.bodyTypeName = bodyTypeName;
	}
	
	public ServiceSpec getServiceSpec() {
		return this.serviceSpec;
	}
	
	public HttpStatus getStatus() {
		return this.status;
	}

	public HttpHeaders getHeaders() {
		return this.headers;
	}

	public Sr getBody() {
		return this.body;
	}
	
	public WebClientResponseException getException() {
		return this.exception;
	}
	
	public String getBodyTypeName() {
		return this.bodyTypeName;
	}


}
