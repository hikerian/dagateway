package dagateway.server.transform.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.service.ServiceFault;
import dagateway.api.transform.AbstractDataTransformer;



/**
 * @author Dong-il Cho
 */
public class TextPlainDataTransformer extends AbstractDataTransformer<String, String> {
	private final Logger log = LoggerFactory.getLogger(TextPlainDataTransformer.class);
	
	public static final String ARGUMENT_TYPE = "java.lang.String";
	public static final String RETURN_TYPE = "java.lang.String";
	
	
	public TextPlainDataTransformer() {
		
	}
	
	@Override
	protected void doInit() {
	}

	@Override
	public String transform(String payload) {
		this.log.debug("transform");

		return payload;
	}

	@Override
	public String transform(ServiceFault fault) {
		return fault.toString();
	}


}
