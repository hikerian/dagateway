package dagateway.api.inserter;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;

import reactor.core.publisher.Flux;


public class DoubleFluxDataBufferInserterBuilder extends AbstractBodyInserterBuilder<Flux<Flux<DataBuffer>>, ReactiveHttpOutputMessage> {
	

	public DoubleFluxDataBufferInserterBuilder() {
	}

	@Override
	public BodyInserter<?, ReactiveHttpOutputMessage> getBodyInserter(Flux<Flux<DataBuffer>> data) {
		return (outputMessage, context) -> {
			return outputMessage.writeAndFlushWith(data);
		};
	}

}
