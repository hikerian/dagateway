package dagateway.server.handler;

import org.springframework.core.io.buffer.DataBuffer;

import dagateway.api.handler.AbstractContentHandler;
import reactor.core.publisher.Flux;



public class MultiDataBufferHandler extends AbstractContentHandler<Flux<DataBuffer>, DataBuffer, DataBuffer, DataBuffer, Flux<DataBuffer>> {
	
	
	public MultiDataBufferHandler() {
	}
	
	@Override
	public Flux<DataBuffer> handle(Flux<DataBuffer> requestBody) {
		// Transform
		Flux<DataBuffer> transformed = requestBody.handle((buffer, sink) -> {
			DataBuffer newBuffer = this.transformer.transform(buffer);
			if(newBuffer != null) {
				sink.next(newBuffer);
			}
		});
		
		return transformed;
	}


}
