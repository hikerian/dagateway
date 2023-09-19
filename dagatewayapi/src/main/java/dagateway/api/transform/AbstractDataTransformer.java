package dagateway.api.transform;

import dagateway.api.context.RouteRequestContext.TransformSpec;


/**
 * json to json hard coding?
 * filter? map? path-> keep -> draw -> tree
 * structural data convertor -> path -> hit or keep or trash
 * source analisys - stream to target structure
 */
public abstract class AbstractDataTransformer<T, V> implements DataTransformer<T, V> {
	private TransformSpec rule;
	
	
	public AbstractDataTransformer() {
	}
	
	public void init(TransformSpec rule) {
		this.rule = rule;
		this.doInit();
	}
	
	protected void doInit() {
	}
	
	protected TransformSpec getTransformSpec() {
		return this.rule;
	}

	@Override
	public abstract V transform(T source);

	
}
