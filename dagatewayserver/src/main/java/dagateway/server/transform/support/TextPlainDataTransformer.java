package dagateway.server.transform.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.service.ServiceFault;
import dagateway.api.transform.AbstractDataTransformer;



public class TextPlainDataTransformer extends AbstractDataTransformer<String, String> {
	private final Logger log = LoggerFactory.getLogger(TextPlainDataTransformer.class);
	
	
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
