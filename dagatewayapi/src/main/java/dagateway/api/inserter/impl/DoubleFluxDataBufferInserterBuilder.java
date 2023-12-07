package dagateway.api.inserter.impl;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;

import dagateway.api.inserter.AbstractBodyInserterBuilder;
import reactor.core.publisher.Flux;



/**
 * @author Dong-il Cho
 */
public class DoubleFluxDataBufferInserterBuilder extends AbstractBodyInserterBuilder<Flux<Flux<DataBuffer>>, ReactiveHttpOutputMessage> {
	

	public DoubleFluxDataBufferInserterBuilder() {
	}

	@Override
	public BodyInserter<?, ReactiveHttpOutputMessage> getBodyInserter(Flux<Flux<DataBuffer>> data) {
		return (outputMessage, context) -> {
			return outputMessage.writeAndFlushWith(data);
		};
	}
	
	@Override
	public String supportType() {
		return "reactor.core.publisher.Flux<reactor.core.publisher.Flux<org.springframework.core.io.buffer.DataBuffer>>";
	}

}
