package dagateway.server.handler.response;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.BodyBuilder;

import dagateway.api.handler.AbstractServiceResponseHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public class MultiDataBufferResponseHandler extends AbstractServiceResponseHandler<Flux<DataBuffer>, DataBuffer, DataBuffer> {
	
	
	public MultiDataBufferResponseHandler() {
	}

	@Override
	protected Flux<DataBuffer> resolveBody(Flux<DataBuffer> responseBody) {
		Flux<DataBuffer> transformed = responseBody.handle((buffer, sink) -> {
			DataBuffer newBuffer = this.transformer.transform(buffer);
			if(newBuffer != null) {
				sink.next(newBuffer);
			}
		});
		
		return transformed;
	}

	@Override
	public Mono<ServerResponse> buildBody(BodyBuilder builder, Flux<DataBuffer> body) {
		return builder.body((outputMessage, context) -> outputMessage.writeAndFlushWith(Mono.just(body)));
	}

}
