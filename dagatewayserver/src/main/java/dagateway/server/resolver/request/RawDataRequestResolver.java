package dagateway.server.resolver.request;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;

import dagateway.api.resolver.MultiRequestDataResolver;
import reactor.core.publisher.Flux;



public class RawDataRequestResolver extends MultiRequestDataResolver<DataBuffer> {
	
	
	public RawDataRequestResolver() {
	}

	@Override
	public Flux<DataBuffer> doResolve(ServerRequest serverRequest) {
		return serverRequest.body(BodyExtractors.toDataBuffers());
	}

	@Override
	public DataBuffer emptyValue() {
		return DefaultDataBufferFactory.sharedInstance.allocateBuffer(0);
	}


}