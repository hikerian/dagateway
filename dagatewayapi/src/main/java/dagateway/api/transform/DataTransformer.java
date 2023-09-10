package dagateway.api.transform;

import dagateway.api.context.RouteContext.TransformSpec;


/**
 * 
 * @author chodo
 *
 * @param <T> inputType
 * @param <V> outputType
 */
public interface DataTransformer<T, V> {
	public void init(TransformSpec rule);
	public V transform(T source);

}
