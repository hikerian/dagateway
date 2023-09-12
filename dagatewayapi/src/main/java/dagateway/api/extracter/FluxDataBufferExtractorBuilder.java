package dagateway.api.extracter;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;

import reactor.core.publisher.Flux;


public class FluxDataBufferExtractorBuilder extends AbstractBodyExtractorBuilder<Flux<DataBuffer>, ReactiveHttpInputMessage> {
	
	
	public FluxDataBufferExtractorBuilder() {
	}

	@Override
	public BodyExtractor<Flux<DataBuffer>, ReactiveHttpInputMessage> getBodyExtractor() {
		return BodyExtractors.toDataBuffers();
	}

}
