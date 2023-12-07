package dagateway.server.transform.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.service.ServiceFault;
import dagateway.api.transform.AbstractDataTransformer;


public class NoneDataTransformer extends AbstractDataTransformer<Void, Void> {
	private final Logger log = LoggerFactory.getLogger(NoneDataTransformer.class);
	
	public static final String ARGUMENT_TYPE = "java.lang.Void";
	public static final String RETURN_TYPE = "java.lang.Void";
	
	
	public NoneDataTransformer() {
	}
	
	@Override
	protected void doInit() {
	}

	@Override
	public Void transform(Void source) {
		this.log.debug("transform");
		return null;
	}

	@Override
	public Void transform(ServiceFault fault) {
		return null;
	}


}
