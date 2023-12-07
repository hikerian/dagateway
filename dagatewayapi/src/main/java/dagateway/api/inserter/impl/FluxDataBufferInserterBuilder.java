package dagateway.api.inserter.impl;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;

import dagateway.api.inserter.AbstractBodyInserterBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class FluxDataBufferInserterBuilder extends AbstractBodyInserterBuilder<Flux<DataBuffer>, ReactiveHttpOutputMessage> {
	
	
	public FluxDataBufferInserterBuilder() {
	}

	@Override
	public BodyInserter<?, ReactiveHttpOutputMessage> getBodyInserter(Flux<DataBuffer> data) {
		return (outputMessage, context) -> {
			return outputMessage.writeAndFlushWith(Mono.just(data));
		};
	}
	
	@Override
	public String supportType() {
		return "reactor.core.publisher.Flux<org.springframework.core.io.buffer.DataBuffer>";
	}

}
