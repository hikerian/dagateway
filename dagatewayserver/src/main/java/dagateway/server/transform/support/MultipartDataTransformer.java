package dagateway.server.transform.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.Part;

import dagateway.api.transform.AbstractDataTransformer;



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

}
