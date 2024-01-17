package dagateway.server.transform;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import dagateway.api.service.ServiceFault;
import dagateway.api.transform.AbstractDataTransformer;
import dagateway.api.transform.DividedDataSupport;



/**
 * @author Dong-il Cho
 */
public class PassDataTransformer extends AbstractDataTransformer<DataBuffer, DataBuffer> implements DividedDataSupport {
	private final Logger log = LoggerFactory.getLogger(PassDataTransformer.class);
	
	public static final String ARGUMENT_TYPE = "org.springframework.core.io.buffer.DataBuffer";
	public static final String RETURN_TYPE = "org.springframework.core.io.buffer.DataBuffer";
	
	
	public PassDataTransformer() {
	}
	
	@Override
	protected void doInit() {
	}

	@Override
	public DataBuffer transform(DataBuffer source) {
		this.log.debug("transform");
		return source;
	}

	@Override
	public DataBuffer transform(ServiceFault fault) {
		String json = fault.toString();
		return DefaultDataBufferFactory.sharedInstance.wrap(json.getBytes(StandardCharsets.UTF_8));
	}


}
