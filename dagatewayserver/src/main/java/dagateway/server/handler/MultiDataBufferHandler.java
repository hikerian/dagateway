package dagateway.server.handler;

import org.springframework.core.io.buffer.DataBuffer;

import dagateway.api.handler.AbstractContentHandler;
import reactor.core.publisher.Flux;



public class MultiDataBufferHandler extends AbstractContentHandler<Flux<DataBuffer>, DataBuffer, DataBuffer, DataBuffer, Flux<DataBuffer>> {
	public static final String ARGUMENT_TYPE = "reactor.core.publisher.Flux<org.springframework.core.io.buffer.DataBuffer>";
	public static final String RETURN_TYPE = "reactor.core.publisher.Flux<org.springframework.core.io.buffer.DataBuffer>";
	public static final String TRANS_ARGUMENT_TYPE = "org.springframework.core.io.buffer.DataBuffer";
	public static final String TRANS_RETURN_TYPE = "org.springframework.core.io.buffer.DataBuffer";
	
	
	public MultiDataBufferHandler() {
	}
	
	@Override
	public String getArgumentTypeName() {
		return ARGUMENT_TYPE;
	}

	@Override
	public String getReturnTypeName() {
		return RETURN_TYPE;
	}

	@Override
	public String getTransArgumentTypeName() {
		return TRANS_ARGUMENT_TYPE;
	}

	@Override
	public String getTransReturnTypeName() {
		return TRANS_RETURN_TYPE;
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

	@Override
	protected Flux<DataBuffer> wrapSingle(DataBuffer value) {
		return Flux.just(value);
	}


}
