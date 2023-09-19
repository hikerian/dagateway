package dagateway.server.transform.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.transform.AbstractDataTransformer;



public class BinaryDataTransformer extends AbstractDataTransformer<byte[], byte[]> {
	private final Logger log = LoggerFactory.getLogger(BinaryDataTransformer.class);
	
	
	public BinaryDataTransformer() {
		
	}
	
	@Override
	protected void doInit() {
	}

	@Override
	public byte[] transform(byte[] payload) {
//		this.log.debug("transform");
		// TODO Auto-generated method stub
		return payload;
	}

}
