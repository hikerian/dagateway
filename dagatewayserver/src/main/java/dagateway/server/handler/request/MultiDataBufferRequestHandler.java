package dagateway.server.handler.request;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import dagateway.api.context.RouteContext;
import dagateway.api.handler.AbstractServiceRequestHandler;
import reactor.core.publisher.Flux;



public class MultiDataBufferRequestHandler extends AbstractServiceRequestHandler<Flux<DataBuffer>, DataBuffer, DataBuffer, DataBuffer> {
	
	
	public MultiDataBufferRequestHandler() {
	}
	
	/**
	 * DataType에 따라서 다양하게 지원 필요.
	 * Mono/Flux가 아닌 Value로 처리...
	 * @param requestBody
	 * @return
	 */
	public RequestHeadersSpec<?> resolveBody(Flux<DataBuffer> requestBody, RequestBodySpec requestBodySpec, RouteContext.ServiceSpec serviceSpec) {
		// Transform
		Flux<DataBuffer> transformed = requestBody.handle((buffer, sink) -> {
			DataBuffer newBuffer = this.transformer.transform(buffer);
			if(newBuffer != null) {
				sink.next(newBuffer);
			}
		});

		return requestBodySpec.body(BodyInserters.fromDataBuffers(transformed));
	}

}
