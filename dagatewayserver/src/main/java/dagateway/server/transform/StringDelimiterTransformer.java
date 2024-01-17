package dagateway.server.transform;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.service.ServiceFault;
import dagateway.api.transform.AbstractDataTransformer;
import dagateway.api.transform.DividedDataSupport;
import dagateway.api.transform.StringDelimiterSupport;



/**
 * @author Dong-il Cho
 */
public class StringDelimiterTransformer extends AbstractDataTransformer<String, String> implements StringDelimiterSupport, DividedDataSupport {
	private final Logger log = LoggerFactory.getLogger(StringDelimiterTransformer.class);
	
	private final List<String> delimiters = StringDelimiterSupport.DEFAULT_DELIMITERS;
	
	public static final String ARGUMENT_TYPE = "java.lang.String";
	public static final String RETURN_TYPE = "java.lang.String";
	
	
	public StringDelimiterTransformer() {
	}
	
	@Override
	protected void doInit() {
	}

	@Override
	public String transform(String source) {
		this.log.debug(source);
		
		return source;
	}
	
	@Override
	public List<String> delimiters() {
		return this.delimiters;
	}

	@Override
	public boolean stripDelimiter() {
		return false;
	}

	@Override
	public String transform(ServiceFault fault) {
		return fault.toString();
	}


}
