package dagateway.api.service;


/**
 * @author Dong-il Cho
 */
public interface ServiceExceptionResolver {
	public ServiceFault resolve(Throwable t);

}
