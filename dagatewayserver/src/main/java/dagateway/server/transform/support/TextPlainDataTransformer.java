package dagateway.server.transform.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
//		this.log.debug("transform");
		// TODO Auto-generated method stub
		return payload;
	}
}
