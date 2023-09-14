package dagateway.server.transform.support;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.transform.AbstractDataTransformer;
import dagateway.api.transform.DividedDataSupport;
import dagateway.api.transform.StringDelimiterSupport;



public class StringDelimiterTransformer extends AbstractDataTransformer<String, String> implements StringDelimiterSupport, DividedDataSupport {
	private final Logger log = LoggerFactory.getLogger(StringDelimiterTransformer.class);
	
	private final List<String> delimiters = StringDelimiterSupport.DEFAULT_DELIMITERS;
	
	
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


}
