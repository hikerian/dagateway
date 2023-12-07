package dagateway.api.inserter;

import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;


public interface BodyInserterBuilder<P, M extends ReactiveHttpOutputMessage> {
	public BodyInserter<?, M> getBodyInserter(P data);
	public String supportType();
}
