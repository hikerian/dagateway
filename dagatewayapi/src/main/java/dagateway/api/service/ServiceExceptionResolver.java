package dagateway.api.service;

public interface ServiceExceptionResolver {
	public ServiceFault resolve(Throwable t);

}
