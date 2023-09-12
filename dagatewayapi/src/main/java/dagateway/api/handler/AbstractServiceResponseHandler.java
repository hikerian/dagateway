package dagateway.api.handler;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import dagateway.api.context.RouteContext;
import dagateway.api.service.ServiceResult;
import dagateway.api.transform.DataTransformer;
import reactor.core.publisher.Flux;


public abstract class AbstractServiceResponseHandler<Sr, T, V> implements ServiceResponseHandler<Sr, T, V> {
	protected DataTransformer<T, V> transformer;
	protected MediaType backendContentType;
	protected MediaType clientContentType;
	
	
	protected AbstractServiceResponseHandler() {
	}
	
	public void init(DataTransformer<T, V> transformer, MediaType backendContentType, MediaType clientContentType) {
		this.transformer = transformer;
		this.backendContentType = backendContentType;
		this.clientContentType = clientContentType;
	}
	
	@Override
	public ServiceResult<Sr> resolve(ResponseEntity<Flux<DataBuffer>> responseEntity, RouteContext.ServiceSpec serviceSpec) {
		Sr body = this.resolveBody(responseEntity.getBody());

		HttpStatus status = responseEntity.getStatusCode();
		HttpHeaders headers = responseEntity.getHeaders();
		HttpHeaders newHttpHeaders = new HttpHeaders();
		newHttpHeaders.putAll(headers);
		newHttpHeaders.setContentType(this.clientContentType);
		
		return new ServiceResult<Sr>(serviceSpec, status, newHttpHeaders, body, this);
	}
	
	protected abstract Sr resolveBody(Flux<DataBuffer> responseBody);

}
