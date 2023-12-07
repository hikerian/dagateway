package dagateway.server.transform.support;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;

import dagateway.api.service.ServiceFault;
import dagateway.api.transform.AbstractDataTransformer;
import dagateway.api.utils.ModifiablePart;
import reactor.core.publisher.Flux;



public class MultipartDataTransformer extends AbstractDataTransformer<Part, Part> {
	private final Logger log = LoggerFactory.getLogger(MultipartDataTransformer.class);
	
	
	public MultipartDataTransformer() {
	}
	
	@Override
	protected void doInit() {
	}

	@Override
	public Part transform(Part source) {
		this.log.debug("transform:" + source.name() + "=" + source.headers());

		return source;
	}

	@Override
	public Part transform(ServiceFault fault) {
		String json = fault.toString();
		DataBuffer dataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(json.getBytes(StandardCharsets.UTF_8));
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		ModifiablePart part = new ModifiablePart("fault", headers, Flux.just(dataBuffer));
		
		return part;
	}


}
