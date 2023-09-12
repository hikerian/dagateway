package dagateway.api.inserter;

import org.springframework.http.ReactiveHttpOutputMessage;


public abstract class AbstractBodyInserterBuilder<P, M extends ReactiveHttpOutputMessage> implements BodyInserterBuilder<P, M> {
	
	protected AbstractBodyInserterBuilder() {
	}

}
