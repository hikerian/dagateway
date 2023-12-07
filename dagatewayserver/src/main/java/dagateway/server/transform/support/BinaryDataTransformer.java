package dagateway.server.transform.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.service.ServiceFault;
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
		this.log.debug("transform");
		return payload;
	}

	@Override
	public byte[] transform(ServiceFault fault) {
		// TODO Is this really acceptable?
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(bout);
			
			oout.writeObject(fault);
			oout.flush();
			oout.close();
			
			byte[] objectbytes = bout.toByteArray();
			
			return objectbytes;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

}
