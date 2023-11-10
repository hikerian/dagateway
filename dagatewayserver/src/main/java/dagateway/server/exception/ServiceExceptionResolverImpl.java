package dagateway.server.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import dagateway.api.service.ServiceExceptionResolver;
import dagateway.api.service.ServiceFault;


public class ServiceExceptionResolverImpl implements ServiceExceptionResolver {
	private final Logger log = LoggerFactory.getLogger(ServiceExceptionResolverImpl.class);
	
	
	public ServiceExceptionResolverImpl() {
	}

	@Override
	public ServiceFault resolve(Throwable t) {
		this.log.debug("resolve", t);
		
		ServiceFaultImpl fault = new ServiceFaultImpl();
		
		if(t instanceof WebClientRequestException) {
			fault.setCode("request");
			fault.setMessage("request-failed");
		} else if(t instanceof WebClientResponseException) {
			fault.setCode("response");
			fault.setMessage("unexpected-response");
		} else {
			fault.setCode("other");
			fault.setMessage("interal-server-error");
		}
		
		fault.setCause(t.toString());
		
		return fault;
	}

}
