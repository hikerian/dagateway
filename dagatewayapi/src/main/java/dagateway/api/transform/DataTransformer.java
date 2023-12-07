package dagateway.api.transform;

import dagateway.api.context.RouteRequestContext.TransformSpec;
import dagateway.api.service.ServiceFault;


/**
 * 
 * @author Dong-il Cho
 *
 * @param <T> inputType
 * @param <V> outputType
 */
public interface DataTransformer<T, V> {
	public void init(TransformSpec rule);
	public V transform(T source);
	public V transform(ServiceFault fault);

}
