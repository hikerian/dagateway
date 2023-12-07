package dagateway.server.transform.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;

import dagateway.api.service.ServiceFault;
import dagateway.api.transform.AbstractDataTransformer;



public class TextEventStreamTransformer extends AbstractDataTransformer<ServerSentEvent<String>, ServerSentEvent<String>> {
	private final Logger log = LoggerFactory.getLogger(TextEventStreamTransformer.class);
	
	
	public TextEventStreamTransformer() {
		
	}
	
	@Override
	protected void doInit() {
	}

	@Override
	public ServerSentEvent<String> transform(ServerSentEvent<String> source) {
		this.log.debug(source.toString());
		
		return source;
	}

	@Override
	public ServerSentEvent<String> transform(ServiceFault fault) {
		ServerSentEvent.Builder<String> builder = ServerSentEvent.builder(fault.toString());
		builder.event("fault");
		
		return builder.build();
	}


}
