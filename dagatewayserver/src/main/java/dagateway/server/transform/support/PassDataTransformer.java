package dagateway.server.transform.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;

import dagateway.api.transform.AbstractDataTransformer;
import dagateway.api.transform.DividedDataSupport;



public class PassDataTransformer extends AbstractDataTransformer<DataBuffer, DataBuffer> implements DividedDataSupport {
	private final Logger log = LoggerFactory.getLogger(PassDataTransformer.class);
	
	
	public PassDataTransformer() {
	}
	
	@Override
	protected void doInit() {
	}

	@Override
	public DataBuffer transform(DataBuffer source) {
//		this.log.debug("transform");
		
		return source;
	}

}
